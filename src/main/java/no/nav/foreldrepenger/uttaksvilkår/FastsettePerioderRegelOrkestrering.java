package no.nav.foreldrepenger.uttaksvilkår;

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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttaksperiode;
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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Trekkdagertilstand;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelsePeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ValgAvStønadskontoTjeneste;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.TomKontoIdentifiserer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.UtfallType;
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

        FastsettePeriodeRegel fastsettePeriodeRegel = new FastsettePeriodeRegel(konfigurasjon, featureToggles);
        OrkestreringTillegg orkestreringTillegg = lagOrkestreringTillegg(grunnlag, konfigurasjon);

        List<UttakPeriode> allePerioderSomSkalFastsettes = samletUttaksperioder(grunnlag, orkestreringTillegg)
                .stream()
                .filter(periode -> !erHelg(periode))
                .filter(periode -> !oppholdSomFyllesAvAnnenpart(periode, annenpartUttaksperioder(grunnlag)))
                .collect(Collectors.toList());

        Trekkdagertilstand trekkdagerTilstand;
        if (grunnlag.getBehandling().isTapende()) {
            trekkdagerTilstand = Trekkdagertilstand.forTapendeBehandling(grunnlag, allePerioderSomSkalFastsettes);
        } else {
            trekkdagerTilstand = Trekkdagertilstand.ny(grunnlag, allePerioderSomSkalFastsettes);
        }

        List<FastsettePeriodeResultat> resultatPerioder = new ArrayList<>();
        for (UttakPeriode aktuellPeriode : allePerioderSomSkalFastsettes) {
            FastsettePeriodeResultat resultat;
            do {
                resultat = fastsettPeriode(fastsettePeriodeRegel, konfigurasjon, grunnlag, trekkdagerTilstand, aktuellPeriode);
                resultatPerioder.add(resultat);
                if (resultat.harFørtTilKnekk()) {
                    aktuellPeriode = resultat.getPeriodeEtterKnekk();
                }
            } while (resultat.harFørtTilKnekk());
        }

        //Bare for å sikre rekkefølge
        return sortByFom(resultatPerioder);
    }

    private boolean erHelg(UttakPeriode periode) {
        return periode.virkedager() == 0;
    }

    private boolean oppholdSomFyllesAvAnnenpart(UttakPeriode periode, List<AnnenpartUttaksperiode> annenpartUttak) {
        if (!erOppholdsperiode(periode)) {
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
        return ap.getUttakPeriodeAktiviteter().stream().anyMatch(a -> a.getTrekkdager().merEnn0());
    }

    private boolean erOppholdsperiode(UttakPeriode periode) {
        return periode instanceof OppholdPeriode && !((OppholdPeriode) periode).getOppholdårsaktype().equals(Oppholdårsaktype.MANGLENDE_SØKT_PERIODE);
    }

    private List<FastsettePeriodeResultat> sortByFom(List<FastsettePeriodeResultat> resultatPerioder) {
        return resultatPerioder.stream().sorted(Comparator.comparing(res -> res.getUttakPeriode().getFom())).collect(Collectors.toList());
    }

    private FastsettePeriodeResultat fastsettPeriode(FastsettePeriodeRegel fastsettePeriodeRegel,
                                                     Konfigurasjon konfigurasjon,
                                                     RegelGrunnlag grunnlag,
                                                     Trekkdagertilstand trekkdagertilstand,
                                                     UttakPeriode aktuellPeriode) {
        FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag = new FastsettePeriodeGrunnlagImpl(grunnlag, trekkdagertilstand, aktuellPeriode);
        RegelResultatBehandler regelResultatBehandler = new RegelResultatBehandlerImpl(trekkdagertilstand, grunnlag, konfigurasjon);
        fastsettePeriodeGrunnlag.getTrekkdagertilstand().trekkSaldoForAnnenPartsPerioder(fastsettePeriodeGrunnlag.getAktuellPeriode());

        Evaluation evaluering = fastsettePeriodeRegel.evaluer(fastsettePeriodeGrunnlag);
        String inputJson = toJson(fastsettePeriodeGrunnlag);
        String regelJson = EvaluationSerializer.asJson(evaluering);
        RegelResultatBehandlerResultat regelResultatBehandlerResultat = behandleRegelresultat(evaluering, aktuellPeriode,
                regelResultatBehandler, grunnlag, konfigurasjon, trekkdagertilstand);

        return new FastsettePeriodeResultat(regelResultatBehandlerResultat.getPeriode(), regelJson, inputJson, regelResultatBehandlerResultat.getEtterKnekkPeriode());
    }

    private List<UttakPeriode> samletUttaksperioder(RegelGrunnlag grunnlag, OrkestreringTillegg orkestreringTillegg) {
        List<UttakPeriode> samlet = new ArrayList<>(grunnlag.getSøknad().getUttaksperioder());
        samlet.addAll(orkestreringTillegg.getOppholdsperioder());

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
        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, konfigurasjon);
        return new OrkestreringTillegg(OppholdPeriodeTjeneste.finnOppholdsperioder(grunnlag, konfigurasjon), knekkpunkter);
    }

    private RegelResultatBehandlerResultat behandleRegelresultat(Evaluation evaluering,
                                                                 UttakPeriode aktuellPeriode,
                                                                 RegelResultatBehandler behandler,
                                                                 RegelGrunnlag regelGrunnlag,
                                                                 Konfigurasjon konfig,
                                                                 Trekkdagertilstand trekkdagertilstand) {
        final RegelResultatBehandlerResultat regelResultatBehandlerResultat;
        Regelresultat regelresultat = new Regelresultat(evaluering);
        settSluttpunktTrekkerDagerPåAlleAktiviteter(aktuellPeriode, regelGrunnlag, regelresultat);
        UtfallType utfallType = regelresultat.getUtfallType();

        UtfallType graderingUtfall = regelresultat.getGradering();
        boolean avslåGradering = UtfallType.AVSLÅTT.equals(graderingUtfall);
        GraderingIkkeInnvilgetÅrsak graderingIkkeInnvilgetÅrsak = regelresultat.getGraderingIkkeInnvilgetÅrsak();
        Optional<TomKontoKnekkpunkt> knekkpunktOpt = finnKnekkpunkt(aktuellPeriode, regelGrunnlag, konfig, trekkdagertilstand);

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
                                                             RegelGrunnlag regelGrunnlag,
                                                             Regelresultat regelresultat) {
        var aktiviteter = regelGrunnlag.getArbeid().getAktiviteter();
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
                                                        Trekkdagertilstand trekkdagertilstand) {
        if (aktuellPeriode instanceof UtsettelsePeriode || erFPFF(aktuellPeriode)) {
            return Optional.empty();
        }
        var stønadskontotype = utledKonto(aktuellPeriode, regelGrunnlag, trekkdagertilstand, konfig);
        return TomKontoIdentifiserer.identifiser(aktuellPeriode, new ArrayList<>(regelGrunnlag.getArbeid().getAktiviteter()),
                trekkdagertilstand, stønadskontotype);
    }

    private Stønadskontotype utledKonto(UttakPeriode aktuellPeriode, RegelGrunnlag regelGrunnlag, Trekkdagertilstand trekkdagertilstand, Konfigurasjon konfig) {
        if (Stønadskontotype.UKJENT.equals(aktuellPeriode.getStønadskontotype())) {
            return ValgAvStønadskontoTjeneste.velgStønadskonto(aktuellPeriode, regelGrunnlag, trekkdagertilstand, konfig).orElse(Stønadskontotype.UKJENT);
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

}
