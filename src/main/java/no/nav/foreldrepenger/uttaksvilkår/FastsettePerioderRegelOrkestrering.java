package no.nav.foreldrepenger.uttaksvilkår;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.saldo.SaldoUtregningTjeneste.lagUtregning;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.foreldrepenger.regler.uttak.Regelresultat;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeRegel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttaksperiode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagImpl;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Oppholdårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OrkestreringTillegg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelResultatBehandler;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelResultatBehandlerImpl;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelResultatBehandlerResultat;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.TomKontoKnekkpunkt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelsePeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ValgAvStønadskontoTjeneste;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.saldo.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.saldo.SaldoUtregning;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.saldo.SaldoUtregningGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.saldo.FastsattUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.TomKontoIdentifiserer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.UtfallType;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Periode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.FeatureToggles;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;
import no.nav.foreldrepenger.uttaksvilkår.feil.UttakRegelFeil;
import no.nav.foreldrepenger.uttaksvilkår.jackson.JacksonJsonConfig;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;

public class FastsettePerioderRegelOrkestrering {

    private JacksonJsonConfig jacksonJsonConfig = new JacksonJsonConfig();

    public List<FastsettePeriodeResultat> fastsettePerioder(RegelGrunnlag grunnlag, FeatureToggles featureToggles) {
        return fastsettePerioder(grunnlag, StandardKonfigurasjon.KONFIGURASJON, featureToggles);
    }

    public List<FastsettePeriodeResultat> fastsettePerioder(RegelGrunnlag grunnlag, Konfigurasjon konfigurasjon, FeatureToggles featureToggles) {

        var fastsettePeriodeRegel = new FastsettePeriodeRegel(konfigurasjon, featureToggles);
        var orkestreringTillegg = lagOrkestreringTillegg(grunnlag, konfigurasjon);

        var allePerioderSomSkalFastsettes = samletUttaksperioder(grunnlag, orkestreringTillegg)
                .stream()
                .filter(periode -> !erHelg(periode))
                .filter(periode -> !oppholdSomFyllesAvAnnenpart(periode, annenpartUttaksperioder(grunnlag)))
                .map(periode -> oppdaterMedAktiviteter(periode, grunnlag.getArbeid()))
                .collect(Collectors.toList());

        var resultatPerioder = new ArrayList<FastsettePeriodeResultat>();
        for (var aktuellPeriode : allePerioderSomSkalFastsettes) {
            FastsettePeriodeResultat resultat;
            do {
                var saldoUtregningGrunnlag = saldoGrunnlag(grunnlag, resultatPerioder, aktuellPeriode, allePerioderSomSkalFastsettes);
                var saldoUtregning = lagUtregning(saldoUtregningGrunnlag);
                resultat = fastsettPeriode(fastsettePeriodeRegel, konfigurasjon, grunnlag, aktuellPeriode, saldoUtregning);
                resultatPerioder.add(resultat);
                if (resultat.harFørtTilKnekk()) {
                    aktuellPeriode = resultat.getPeriodeEtterKnekk();
                }
            } while (resultat.harFørtTilKnekk());
        }

        //Bare for å sikre rekkefølge
        return sortByFom(resultatPerioder);
    }

    private UttakPeriode oppdaterMedAktiviteter(UttakPeriode periode, Arbeid arbeid) {
        var aktiviteter = aktiviteterIPeriode(periode, arbeid);
        periode.setAktiviteter(aktiviteter);
        return periode;
    }

    private Set<AktivitetIdentifikator> aktiviteterIPeriode(UttakPeriode periode, Arbeid arbeid) {
        return arbeid.getArbeidsforhold().stream()
                .filter(arbeidsforhold -> !arbeidsforhold.getStartdato().isAfter(periode.getFom()))
                .map(Arbeidsforhold::getIdentifikator)
                .collect(Collectors.toSet());
    }

    private boolean erHelg(UttakPeriode periode) {
        return periode.virkedager() == 0;
    }

    private boolean oppholdSomFyllesAvAnnenpart(UttakPeriode periode, List<AnnenpartUttaksperiode> annenpartUttak) {
        if (!(periode instanceof OppholdPeriode)) {
            return false;
        }
        return annenpartUttak.stream()
                .filter(ap -> ap.overlapper(periode))
                .anyMatch(ap -> harTrekkdager(ap) || innvilgetUtsettelse(ap));
    }

    private boolean innvilgetUtsettelse(AnnenpartUttaksperiode ap) {
        return ap.isInnvilget() && ap.isUtsettelse();
    }

    private boolean harTrekkdager(AnnenpartUttaksperiode ap) {
        return ap.getAktiviteter().stream().anyMatch(a -> a.getTrekkdager().merEnn0());
    }

    private List<FastsettePeriodeResultat> sortByFom(List<FastsettePeriodeResultat> resultatPerioder) {
        return resultatPerioder.stream().sorted(Comparator.comparing(res -> res.getUttakPeriode().getFom())).collect(Collectors.toList());
    }

    private FastsettePeriodeResultat fastsettPeriode(FastsettePeriodeRegel fastsettePeriodeRegel,
                                                     Konfigurasjon konfigurasjon,
                                                     RegelGrunnlag grunnlag,
                                                     UttakPeriode aktuellPeriode,
                                                     SaldoUtregning saldoUtregning) {
        var fastsettePeriodeGrunnlag = new FastsettePeriodeGrunnlagImpl(grunnlag, saldoUtregning, aktuellPeriode);
        var regelResultatBehandler = new RegelResultatBehandlerImpl(saldoUtregning, grunnlag, konfigurasjon);

        var evaluering = fastsettePeriodeRegel.evaluer(fastsettePeriodeGrunnlag);
        var inputJson = toJson(fastsettePeriodeGrunnlag);
        var regelJson = EvaluationSerializer.asJson(evaluering);
        var regelResultatBehandlerResultat = behandleRegelresultat(evaluering, aktuellPeriode,
                regelResultatBehandler, grunnlag, konfigurasjon, saldoUtregning);

        return new FastsettePeriodeResultat(regelResultatBehandlerResultat.getPeriode(), regelJson, inputJson, regelResultatBehandlerResultat.getEtterKnekkPeriode());
    }

    private List<UttakPeriode> samletUttaksperioder(RegelGrunnlag grunnlag, OrkestreringTillegg orkestreringTillegg) {
        List<UttakPeriode> samlet = new ArrayList<>(grunnlag.getSøknad().getUttaksperioder());
        samlet.addAll(orkestreringTillegg.getManglendeSøktPerioder());

        for (LocalDate knekkpunkt : orkestreringTillegg.getKnekkpunkter()) {
            samlet = knekk(samlet, knekkpunkt);
        }

        return samlet.stream().sorted(Comparator.comparing(Periode::getFom)).collect(Collectors.toList());
    }

    private List<UttakPeriode> knekk(List<UttakPeriode> førKnekk, LocalDate knekkpunkt) {
        List<UttakPeriode> etterKnekk = new ArrayList<>();
        for (UttakPeriode uttakPeriode : førKnekk) {
            if (uttakPeriode.overlapper(knekkpunkt) && !uttakPeriode.getFom().equals(knekkpunkt)) {
                etterKnekk.add(uttakPeriode.kopiMedNyPeriode(uttakPeriode.getFom(), knekkpunkt.minusDays(1)));
                etterKnekk.add(uttakPeriode.kopiMedNyPeriode(knekkpunkt, uttakPeriode.getTom()));
            } else {
                etterKnekk.add(uttakPeriode);
            }
        }

        return etterKnekk;
    }

    private OrkestreringTillegg lagOrkestreringTillegg(RegelGrunnlag grunnlag, Konfigurasjon konfigurasjon) {
        var knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, konfigurasjon);
        var manglendeSøktPerioder = ManglendeSøktePerioderTjeneste.finnManglendeSøktePerioder(grunnlag, konfigurasjon);
        return new OrkestreringTillegg(manglendeSøktPerioder, knekkpunkter);
    }

    private RegelResultatBehandlerResultat behandleRegelresultat(Evaluation evaluering,
                                                                 UttakPeriode aktuellPeriode,
                                                                 RegelResultatBehandler behandler,
                                                                 RegelGrunnlag regelGrunnlag,
                                                                 Konfigurasjon konfig,
                                                                 SaldoUtregning saldoUtregning) {
        final RegelResultatBehandlerResultat regelResultatBehandlerResultat;
        Regelresultat regelresultat = new Regelresultat(evaluering);
        settSluttpunktTrekkerDagerPåAlleAktiviteter(aktuellPeriode, regelresultat);
        UtfallType utfallType = regelresultat.getUtfallType();

        UtfallType graderingUtfall = regelresultat.getGradering();
        boolean avslåGradering = UtfallType.AVSLÅTT.equals(graderingUtfall);
        GraderingIkkeInnvilgetÅrsak graderingIkkeInnvilgetÅrsak = regelresultat.getGraderingIkkeInnvilgetÅrsak();
        Optional<TomKontoKnekkpunkt> knekkpunktOpt = finnKnekkpunkt(aktuellPeriode, regelGrunnlag, konfig, saldoUtregning);

        switch (utfallType) {
            case AVSLÅTT:
                List<AnnenpartUttaksperiode> annenPartUttaksperioder = annenpartUttaksperioder(regelGrunnlag);
                regelResultatBehandlerResultat = behandler.avslåAktuellPeriode(aktuellPeriode, knekkpunktOpt, regelresultat.getAvklaringÅrsak(),
                        regelresultat.skalUtbetale(), overlapperMedInnvilgetAnnenpartsPeriode(aktuellPeriode, annenPartUttaksperioder));
                break;
            case INNVILGET:
                regelResultatBehandlerResultat = behandler.innvilgAktuellPeriode(aktuellPeriode, knekkpunktOpt, regelresultat.getInnvilgetÅrsak(), avslåGradering,
                        graderingIkkeInnvilgetÅrsak, regelresultat.skalUtbetale());
                break;
            case MANUELL_BEHANDLING:
                regelResultatBehandlerResultat = behandler.manuellBehandling(aktuellPeriode, regelresultat.getManuellbehandlingårsak(), regelresultat.getAvklaringÅrsak(),
                        regelresultat.skalUtbetale(), avslåGradering, graderingIkkeInnvilgetÅrsak);
                break;
            default:
                throw new UnsupportedOperationException(String.format("Ukjent utfalltype: %s", utfallType.name()));
        }

        return regelResultatBehandlerResultat;
    }

    private void settSluttpunktTrekkerDagerPåAlleAktiviteter(UttakPeriode aktuellPeriode,
                                                             Regelresultat regelresultat) {
        var aktiviteter = aktuellPeriode.getAktiviteter();
        aktiviteter.forEach(aktivitet -> aktuellPeriode.setSluttpunktTrekkerDager(aktivitet, regelresultat.trekkDagerFraSaldo()));
    }

    private List<AnnenpartUttaksperiode> annenpartUttaksperioder(RegelGrunnlag regelGrunnlag) {
        return regelGrunnlag.getAnnenPart() == null ? Collections.emptyList() : regelGrunnlag.getAnnenPart().getUttaksperioder();
    }

    private boolean overlapperMedInnvilgetAnnenpartsPeriode(UttakPeriode aktuellPeriode, List<AnnenpartUttaksperiode> annenPartUttaksperioder) {
        return annenPartUttaksperioder.stream().anyMatch(annenpartsPeriode -> annenpartsPeriode.overlapper(aktuellPeriode) && annenpartsPeriode.isInnvilget());
    }

    private Optional<TomKontoKnekkpunkt> finnKnekkpunkt(UttakPeriode aktuellPeriode,
                                                        RegelGrunnlag regelGrunnlag,
                                                        Konfigurasjon konfig,
                                                        SaldoUtregning saldoUtregning) {
        if (aktuellPeriode instanceof UtsettelsePeriode || erFPFF(aktuellPeriode)) {
            return Optional.empty();
        }
        var stønadskontotype = utledKonto(aktuellPeriode, regelGrunnlag, saldoUtregning, konfig);
        return TomKontoIdentifiserer.identifiser(aktuellPeriode, new ArrayList<>(aktuellPeriode.getAktiviteter()),
                saldoUtregning, stønadskontotype);
    }

    private Stønadskontotype utledKonto(UttakPeriode aktuellPeriode, RegelGrunnlag regelGrunnlag, SaldoUtregning saldoUtregning, Konfigurasjon konfig) {
        if (Stønadskontotype.UKJENT.equals(aktuellPeriode.getStønadskontotype())) {
            return ValgAvStønadskontoTjeneste.velgStønadskonto(aktuellPeriode, regelGrunnlag, saldoUtregning, konfig).orElse(Stønadskontotype.UKJENT);
        }
        return aktuellPeriode.getStønadskontotype();
    }

    private boolean erFPFF(UttakPeriode aktuellPeriode) {
        return Stønadskontotype.FORELDREPENGER_FØR_FØDSEL.equals(aktuellPeriode.getStønadskontotype());
    }

    private String toJson(FastsettePeriodeGrunnlag grunnlag) {
        try {
            return jacksonJsonConfig.toJson(grunnlag);
        } catch (JsonProcessingException e) {
            throw new UttakRegelFeil("Kunne ikke serialisere regelinput for avklaring av uttaksperioder.", e);
        }
    }

    private SaldoUtregningGrunnlag saldoGrunnlag(RegelGrunnlag grunnlag,
                                                 List<FastsettePeriodeResultat> resultatPerioder,
                                                 UttakPeriode aktuellPeriode,
                                                 List<UttakPeriode> allePerioderSomSkalFastsettes) {
        List<AnnenpartUttaksperiode> annenpartPerioder = grunnlag.getAnnenPart() == null ? List.of() : grunnlag.getAnnenPart().getUttaksperioder();

        var søkersFastsattePerioder = map(resultatPerioder);
        var arbeidsforhold = grunnlag.getArbeid().getArbeidsforhold();
        var utregningsdato = aktuellPeriode.getFom();
        if (grunnlag.getBehandling().isTapende()) {
            var søktePerioder = new ArrayList<LukketPeriode>(allePerioderSomSkalFastsettes);
            return SaldoUtregningGrunnlag.forUtregningAvDelerAvUttakTapendeBehandling(søkersFastsattePerioder,
                    annenpartPerioder, arbeidsforhold, utregningsdato, søktePerioder);
        }
        return SaldoUtregningGrunnlag.forUtregningAvDelerAvUttak(søkersFastsattePerioder, annenpartPerioder,
                arbeidsforhold, utregningsdato);
    }

    private FastsattUttakPeriode map(UttakPeriode periode) {
        return new FastsattUttakPeriode.Builder()
                .medTidsperiode(periode.getFom(), periode.getTom())
                .medAktiviteter(mapAktiviteter(periode))
                .medFlerbarnsdager(periode.isFlerbarnsdager())
                .medOppholdÅrsak(oppholdsårsak(periode))
                .medSamtidigUttak(periode.isSamtidigUttak())
                .medPeriodeResultatType(periode.getPerioderesultattype())
                .build();
    }

    private List<FastsattUttakPeriode> map(List<FastsettePeriodeResultat> perioder) {
        return perioder.stream().map(this::map).collect(Collectors.toList());
    }

    private FastsattUttakPeriode map(FastsettePeriodeResultat fastsattPeriode) {
        var periode = fastsattPeriode.getUttakPeriode();
        return map(periode);
    }

    private Oppholdårsaktype oppholdsårsak(UttakPeriode periode) {
        if (periode instanceof OppholdPeriode) {
            return ((OppholdPeriode) periode).getOppholdårsaktype();
        }
        return null;
    }

    private List<FastsattUttakPeriodeAktivitet> mapAktiviteter(UttakPeriode periode) {
        return periode.getAktiviteter().stream()
                .map(aktivitetIdentifikator -> new FastsattUttakPeriodeAktivitet(periode.getTrekkdager(aktivitetIdentifikator), periode.getStønadskontotype(), aktivitetIdentifikator))
                .collect(Collectors.toList());
    }

}
