package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningTjeneste.lagUtregning;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.regler.feil.UttakRegelFeil;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Revurdering;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Vedtak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregning;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.TomKontoIdentifiserer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.TomKontoKnekkpunkt;
import no.nav.foreldrepenger.regler.uttak.felles.UttakVersion;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Periode;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;
import no.nav.fpsak.nare.evaluation.summary.NareVersion;
import no.nav.fpsak.nare.json.JsonOutput;
import no.nav.fpsak.nare.json.NareJsonException;

public class FastsettePerioderRegelOrkestrering {

    public List<FastsettePeriodeResultat> fastsettePerioder(RegelGrunnlag grunnlag) {

        var fastsettePeriodeRegel = new FastsettePeriodeRegel();
        var orkestreringTillegg = lagOrkestreringTillegg(grunnlag);

        var allePerioderSomSkalFastsettes = samletUttaksperioder(grunnlag, orkestreringTillegg).stream()
                .filter(periode -> !erHelg(periode))
                .filter(periode -> !oppholdSomFyllesAvAnnenpart(periode, annenpartUttaksperioder(grunnlag)))
                .filter(periode -> grunnlag.getBehandling().isKreverSammenhengendeUttak() || !periode.isOpphold())
                .map(periode -> oppdaterMedAktiviteter(periode, grunnlag.getArbeid()))
                .collect(Collectors.toList());
        validerOverlapp(map(allePerioderSomSkalFastsettes));

        var farRundtFødselIntervall = FarUttakRundtFødsel.utledFarsPeriodeRundtFødsel(grunnlag).orElse(null);

        var resultatPerioder = new ArrayList<FastsettePeriodeResultat>();
        for (var aktuellPeriode : allePerioderSomSkalFastsettes) {
            FastsettePeriodeResultat resultat;
            do {
                var saldoUtregningGrunnlag = saldoGrunnlag(grunnlag, resultatPerioder, aktuellPeriode, allePerioderSomSkalFastsettes);
                var saldoUtregning = lagUtregning(saldoUtregningGrunnlag);
                resultat = fastsettPeriode(fastsettePeriodeRegel, grunnlag, aktuellPeriode, saldoUtregning, farRundtFødselIntervall);
                resultatPerioder.add(resultat);
                validerOverlapp(map(resultatPerioder));
                if (resultat.harFørtTilKnekk()) {
                    aktuellPeriode = resultat.getPeriodeEtterKnekk();
                }
            } while (resultat.harFørtTilKnekk());
        }

        //Bare for å sikre rekkefølge
        return sortByFom(resultatPerioder);
    }

    private OppgittPeriode oppdaterMedAktiviteter(OppgittPeriode periode, Arbeid arbeid) {
        var aktiviteter = aktiviteterIPeriode(periode, arbeid);
        periode.setAktiviteter(aktiviteter);
        return periode;
    }

    private Set<AktivitetIdentifikator> aktiviteterIPeriode(OppgittPeriode periode, Arbeid arbeid) {
        var aktiviteter = arbeid.getArbeidsforhold()
                .stream()
                .filter(arbeidsforhold -> arbeid.getArbeidsforhold().size() == 1 || arbeidsforhold.erAktivtPåDato(periode.getFom()))
                .map(Arbeidsforhold::identifikator)
                .collect(Collectors.toSet());
        //Vi kan opprette manglende søkt i en periode som ikke har noe arbeid. Typisk bare far rett og søker ikke fra uke 7
        if (aktiviteter.isEmpty()) {
            var aktivitetMedTidligstStartdato = arbeid.getArbeidsforhold()
                    .stream()
                    .min(Comparator.comparing(Arbeidsforhold::startdato))
                    .map(a -> a.identifikator())
                    .orElseThrow();
            aktiviteter.add(aktivitetMedTidligstStartdato);
        }
        return aktiviteter;
    }

    private boolean erHelg(OppgittPeriode periode) {
        return periode.virkedager() == 0;
    }

    private boolean oppholdSomFyllesAvAnnenpart(OppgittPeriode periode, List<AnnenpartUttakPeriode> annenpartUttak) {
        if (!periode.isOpphold()) {
            return false;
        }
        return annenpartUttak.stream()
                .filter(ap -> ap.overlapper(periode))
                .anyMatch(ap -> harTrekkdager(ap) || innvilgetUtsettelse(ap));
    }

    private boolean innvilgetUtsettelse(AnnenpartUttakPeriode ap) {
        return ap.isInnvilget() && ap.isUtsettelse();
    }

    private boolean harTrekkdager(AnnenpartUttakPeriode ap) {
        return ap.getAktiviteter().stream().anyMatch(a -> a.getTrekkdager().merEnn0());
    }

    private List<FastsettePeriodeResultat> sortByFom(List<FastsettePeriodeResultat> resultatPerioder) {
        return resultatPerioder.stream()
                .sorted(Comparator.comparing(res -> res.getUttakPeriode().getFom()))
                .collect(Collectors.toList());
    }

    private FastsettePeriodeResultat fastsettPeriode(FastsettePeriodeRegel fastsettePeriodeRegel, RegelGrunnlag grunnlag,
                                                     OppgittPeriode aktuellPeriode,
                                                     SaldoUtregning saldoUtregning,
                                                     LukketPeriode farRundtFødselIntervall) {
        var fastsettePeriodeGrunnlag = new FastsettePeriodeGrunnlagImpl(grunnlag, farRundtFødselIntervall, saldoUtregning, aktuellPeriode);
        var regelResultatBehandler = new RegelResultatBehandler(saldoUtregning, grunnlag);

        var evaluering = fastsettePeriodeRegel.evaluer(fastsettePeriodeGrunnlag);
        var inputJson = toJson(fastsettePeriodeGrunnlag);
        var regelJson = EvaluationSerializer.asJson(evaluering, UttakVersion.UTTAK_VERSION, NareVersion.NARE_VERSION);
        var regelResultatBehandlerResultat = behandleRegelresultat(evaluering, fastsettePeriodeGrunnlag, regelResultatBehandler, grunnlag, saldoUtregning);

        return new FastsettePeriodeResultat(regelResultatBehandlerResultat.getPeriode(), regelJson, inputJson,
                regelResultatBehandlerResultat.getEtterKnekkPeriode());
    }

    private List<OppgittPeriode> samletUttaksperioder(RegelGrunnlag grunnlag, OrkestreringTillegg orkestreringTillegg) {
        List<OppgittPeriode> samlet = new ArrayList<>(grunnlag.getSøknad().getOppgittePerioder());
        samlet.addAll(orkestreringTillegg.getManglendeSøktPerioder());

        for (var knekkpunkt : orkestreringTillegg.getKnekkpunkter()) {
            samlet = knekk(samlet, knekkpunkt);
        }

        return samlet.stream().sorted(Comparator.comparing(Periode::getFom)).collect(Collectors.toList());
    }

    private List<OppgittPeriode> knekk(List<OppgittPeriode> førKnekk, LocalDate knekkpunkt) {
        List<OppgittPeriode> etterKnekk = new ArrayList<>();
        for (var oppgittPeriode : førKnekk) {
            if (oppgittPeriode.overlapper(knekkpunkt) && !oppgittPeriode.getFom().equals(knekkpunkt)) {
                etterKnekk.add(oppgittPeriode.kopiMedNyPeriode(oppgittPeriode.getFom(), knekkpunkt.minusDays(1)));
                etterKnekk.add(oppgittPeriode.kopiMedNyPeriode(knekkpunkt, oppgittPeriode.getTom()));
            } else {
                etterKnekk.add(oppgittPeriode);
            }
        }

        return etterKnekk;
    }

    private OrkestreringTillegg lagOrkestreringTillegg(RegelGrunnlag grunnlag) {
        var knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag);
        var manglendeSøktPerioder = finnManglendeSøktePerioder(grunnlag);
        return new OrkestreringTillegg(manglendeSøktPerioder, knekkpunkter);
    }

    private List<OppgittPeriode> finnManglendeSøktePerioder(RegelGrunnlag grunnlag) {
        if (grunnlag.getBehandling().isKreverSammenhengendeUttak()) {
            return ManglendeSøktePerioderForSammenhengendeUttakTjeneste.finnManglendeSøktePerioder(grunnlag);
        }
        return ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag);
    }

    private RegelResultatBehandlerResultat behandleRegelresultat(Evaluation evaluering,
                                                                 FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag,
                                                                 RegelResultatBehandler behandler,
                                                                 RegelGrunnlag regelGrunnlag, SaldoUtregning saldoUtregning) {
        var aktuellPeriode = fastsettePeriodeGrunnlag.getAktuellPeriode();
        var regelresultat = new FastsettePerioderRegelresultat(evaluering);
        var utfallType = regelresultat.getUtfallType();

        var knekkpunktOpt = finnKnekkpunkt(aktuellPeriode, regelGrunnlag, saldoUtregning, regelresultat,
            fastsettePeriodeGrunnlag.periodeFarRundtFødsel().orElse(null));

        var annenpartSamtidigUttaksprosent = SamtidigUttakUtil.kanRedusereUtbetalingsgradForTapende(fastsettePeriodeGrunnlag, regelGrunnlag) ?
            SamtidigUttakUtil.uttaksprosentAnnenpart(fastsettePeriodeGrunnlag) : SamtidigUttaksprosent.ZERO;

        return switch (utfallType) {
            case AVSLÅTT -> behandler.avslåAktuellPeriode(aktuellPeriode, regelresultat, knekkpunktOpt,
                    overlapperMedInnvilgetAnnenpartsPeriode(aktuellPeriode, annenpartUttaksperioder(regelGrunnlag)));
            case INNVILGET -> behandler.innvilgAktuellPeriode(aktuellPeriode, knekkpunktOpt, regelresultat, annenpartSamtidigUttaksprosent);
            case MANUELL_BEHANDLING -> behandler.manuellBehandling(aktuellPeriode, regelresultat);
        };
    }

    private List<AnnenpartUttakPeriode> annenpartUttaksperioder(RegelGrunnlag regelGrunnlag) {
        return regelGrunnlag.getAnnenPart() == null ? Collections.emptyList() : regelGrunnlag.getAnnenPart().getUttaksperioder();
    }

    private boolean overlapperMedInnvilgetAnnenpartsPeriode(OppgittPeriode aktuellPeriode,
                                                            List<AnnenpartUttakPeriode> annenPartUttaksperioder) {
        return annenPartUttaksperioder.stream()
                .anyMatch(annenpartsPeriode -> annenpartsPeriode.overlapper(aktuellPeriode) && annenpartsPeriode.isInnvilget());
    }

    private Optional<TomKontoKnekkpunkt> finnKnekkpunkt(OppgittPeriode aktuellPeriode,
                                                        RegelGrunnlag regelGrunnlag, SaldoUtregning saldoUtregning,
                                                        FastsettePerioderRegelresultat regelresultat,
                                                        LukketPeriode farRundtFødselIntervall) {
        if (erFPFF(aktuellPeriode)) {
            return Optional.empty();
        }
        var stønadskontotype = utledKonto(aktuellPeriode, regelGrunnlag, saldoUtregning);
        var startdatoNesteStønadsperiode = regelGrunnlag.getDatoer().getStartdatoNesteStønadsperiode().orElse(null);
        return TomKontoIdentifiserer.identifiser(aktuellPeriode, new ArrayList<>(aktuellPeriode.getAktiviteter()), saldoUtregning,
                stønadskontotype.orElse(null), farRundtFødselIntervall, startdatoNesteStønadsperiode,
                regelresultat.trekkDagerFraSaldo(), regelresultat.getAvklaringÅrsak(), regelresultat.getUtfallType());
    }

    private Optional<Stønadskontotype> utledKonto(OppgittPeriode aktuellPeriode, RegelGrunnlag regelGrunnlag, SaldoUtregning saldoUtregning) {
        return Optional.ofNullable(aktuellPeriode.getStønadskontotype())
                .or(() -> ValgAvStønadskontoTjeneste.velgStønadskonto(aktuellPeriode, regelGrunnlag, saldoUtregning));
    }

    private boolean erFPFF(OppgittPeriode aktuellPeriode) {
        return Stønadskontotype.FORELDREPENGER_FØR_FØDSEL.equals(aktuellPeriode.getStønadskontotype());
    }

    private String toJson(FastsettePeriodeGrunnlag grunnlag) {
        try {
            return JsonOutput.asJson(grunnlag);
        } catch (NareJsonException e) {
            throw new UttakRegelFeil("Kunne ikke serialisere regelinput for avklaring av uttaksperioder.", e);
        }
    }

    private SaldoUtregningGrunnlag saldoGrunnlag(RegelGrunnlag grunnlag, List<FastsettePeriodeResultat> resultatPerioder,
                                                 OppgittPeriode aktuellPeriode, List<OppgittPeriode> allePerioderSomSkalFastsettes) {
        List<AnnenpartUttakPeriode> annenpartPerioder =
                Optional.ofNullable(grunnlag.getAnnenPart()).map(AnnenPart::getUttaksperioder).orElse(List.of());

        var vedtaksperioder = vedtaksperioder(grunnlag);
        var søkersFastsattePerioder = map(resultatPerioder, vedtaksperioder);
        var utregningsdato = aktuellPeriode.getFom();
        if (grunnlag.getBehandling().isBerørtBehandling()) {
            var søktePerioder = new ArrayList<LukketPeriode>(allePerioderSomSkalFastsettes);
            return SaldoUtregningGrunnlag.forUtregningAvDelerAvUttakBerørtBehandling(søkersFastsattePerioder, annenpartPerioder,
                    grunnlag, utregningsdato, søktePerioder);
        }
        return SaldoUtregningGrunnlag.forUtregningAvDelerAvUttak(søkersFastsattePerioder, annenpartPerioder, grunnlag, utregningsdato);
    }

    private List<FastsattUttakPeriode> vedtaksperioder(RegelGrunnlag grunnlag) {
        return Optional.ofNullable(grunnlag.getRevurdering())
                .map(Revurdering::getGjeldendeVedtak)
                .map(Vedtak::getPerioder)
                .orElse(List.of());
    }

    private FastsattUttakPeriode map(UttakPeriode periode) {
        return new FastsattUttakPeriode.Builder()
                .tidsperiode(periode.getFom(), periode.getTom())
                .aktiviteter(mapAktiviteter(periode))
                .flerbarnsdager(periode.isFlerbarnsdager())
                .resultatÅrsak(mapTilÅrsak(periode.getPeriodeResultatÅrsak()))
                .utsettelse(periode.getUtsettelseÅrsak() != null)
                .oppholdÅrsak(periode.getOppholdÅrsak())
                .samtidigUttak(periode.erSamtidigUttak())
                .periodeResultatType(periode.getPerioderesultattype())
                .build();
    }

    public static FastsattUttakPeriode.ResultatÅrsak mapTilÅrsak(PeriodeResultatÅrsak årsak) {
        if (InnvilgetÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT.equals(årsak)) {
            return FastsattUttakPeriode.ResultatÅrsak.INNVILGET_FORELDREPENGER_KUN_FAR_HAR_RETT;
        }
        if (InnvilgetÅrsak.GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT.equals(årsak)) {
            return FastsattUttakPeriode.ResultatÅrsak.INNVILGET_GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT;
        }
        if (InnvilgetÅrsak.UTSETTELSE_GYLDIG.equals(årsak)) {
            return FastsattUttakPeriode.ResultatÅrsak.UTSETTELSE_GYLDIG;
        }
        if (IkkeOppfyltÅrsak.SØKNADSFRIST.equals(årsak)) {
            return FastsattUttakPeriode.ResultatÅrsak.IKKE_OPPFYLT_SØKNADSFRIST;
        }
        return FastsattUttakPeriode.ResultatÅrsak.ANNET;
    }

    private List<FastsattUttakPeriode> map(List<FastsettePeriodeResultat> resultatPerioder,
                                           List<FastsattUttakPeriode> vedtaksperioder) {
        var fastsattePerioder = new ArrayList<>(vedtaksperioder);
        var mapped = resultatPerioder.stream().map(this::map).toList();
        fastsattePerioder.addAll(mapped);
        return fastsattePerioder;
    }

    private List<LukketPeriode> map(List<OppgittPeriode> allePerioderSomSkalFastsettes) {
        return allePerioderSomSkalFastsettes.stream().map(oppgittPeriode -> (LukketPeriode) oppgittPeriode).toList();
    }

    private void validerOverlapp(List<LukketPeriode> perioder) {
        for (var i = 0; i < perioder.size(); i++) {
            var p1 = perioder.get(i);
            for (var j = i + 1; j < perioder.size(); j++) {
                var p2 = perioder.get(j);
                if (p1.overlapper(p2)) {
                    throw new IllegalStateException("Funnet overlapp i perioder " + p1 + " - " + p2);
                }
            }
        }
    }

    private FastsattUttakPeriode map(FastsettePeriodeResultat fastsattPeriode) {
        var periode = fastsattPeriode.getUttakPeriode();
        return map(periode);
    }

    private List<FastsattUttakPeriodeAktivitet> mapAktiviteter(UttakPeriode periode) {
        return periode.getAktiviteter()
                .stream()
                .map(aktivitet -> new FastsattUttakPeriodeAktivitet(aktivitet.getTrekkdager(), periode.getStønadskontotype(),
                        aktivitet.getIdentifikator()))
                .collect(Collectors.toList());
    }

    private List<LukketPeriode> map(ArrayList<FastsettePeriodeResultat> resultatPerioder) {
        return resultatPerioder.stream().map(p -> (LukketPeriode) p.getUttakPeriode()).toList();
    }
}
