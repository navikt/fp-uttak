package no.nav.foreldrepenger.uttaksvilkår;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.foreldrepenger.regler.uttak.Regelresultat;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeRegel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsprosenter;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandlingtype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattPeriodeAnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagImpl;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.TomKontoKnekkpunkt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OrkestreringTillegg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelResultatBehandler;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelResultatBehandlerImpl;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelResultatBehandlerResultat;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Trekkdagertilstand;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelsePeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
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

        List<UttakPeriode> allePerioderSomSkalFastsettes = filtrereBortHelger(samletUttaksperioder(grunnlag, orkestreringTillegg));

        Trekkdagertilstand trekkdagerTilstand;
        if (Objects.equals(grunnlag.getBehandling().getType(), Behandlingtype.REVURDERING_BERØRT_SAK)) {
            trekkdagerTilstand = Trekkdagertilstand.forBerørtSak(grunnlag);
        } else {
            trekkdagerTilstand = Trekkdagertilstand.ny(grunnlag, allePerioderSomSkalFastsettes);
        }

        fastsettArbeidsprosenter(allePerioderSomSkalFastsettes, grunnlag.getArbeid().getArbeidsprosenter());

        List<FastsettePeriodeResultat> resultatPerioder = new ArrayList<>();
        for (UttakPeriode aktuellPeriode : allePerioderSomSkalFastsettes) {
            FastsettePeriodeResultat resultat = fastsettPeriode(fastsettePeriodeRegel, konfigurasjon, grunnlag, trekkdagerTilstand, aktuellPeriode);
            resultatPerioder.add(resultat);
            if (resultat.harFørtTilKnekk()) {
                FastsettePeriodeResultat etterKnekkResultat = fastsettPeriode(fastsettePeriodeRegel, konfigurasjon, grunnlag, trekkdagerTilstand, resultat.getPeriodeEtterKnekk());
                if (etterKnekkResultat.harFørtTilKnekk()) {
                    throw new IllegalStateException("Forventet ikke at periode har ført til knekk. Den opprinnelige perioder har allerede blitt knekt en gang");
                }
                resultatPerioder.add(etterKnekkResultat);
            }
        }

        //Bare for å sikre rekkefølge
        return sortByFom(resultatPerioder);
    }

    private List<FastsettePeriodeResultat> sortByFom(List<FastsettePeriodeResultat> resultatPerioder) {
        return resultatPerioder.stream().sorted(Comparator.comparing(res -> res.getUttakPeriode().getFom())).collect(Collectors.toList());
    }

    private List<UttakPeriode> filtrereBortHelger(List<UttakPeriode> samletUttaksperioder) {
        List<UttakPeriode> alleUttaksperiodene = new ArrayList<>();
        for (UttakPeriode periode : samletUttaksperioder) {
            if (periode.virkedager() != 0) {
                alleUttaksperiodene.add(periode);
            }
        }
        return alleUttaksperiodene;
    }

    private FastsettePeriodeResultat fastsettPeriode(FastsettePeriodeRegel fastsettePeriodeRegel,
                                                     Konfigurasjon konfigurasjon, RegelGrunnlag grunnlag,
                                                     Trekkdagertilstand trekkdagertilstand,
                                                     UttakPeriode aktuellPeriode) {
        FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag = new FastsettePeriodeGrunnlagImpl(grunnlag, trekkdagertilstand, aktuellPeriode);
        RegelResultatBehandler regelResultatBehandler = new RegelResultatBehandlerImpl(trekkdagertilstand, grunnlag, konfigurasjon);
        fastsettePeriodeGrunnlag.getTrekkdagertilstand().trekkSaldoForAnnenPartsPerioder(fastsettePeriodeGrunnlag.getAktuellPeriode());

        Evaluation evaluering = fastsettePeriodeRegel.evaluer(fastsettePeriodeGrunnlag);
        String inputJson = toJson(fastsettePeriodeGrunnlag);
        String regelJson = EvaluationSerializer.asJson(evaluering);
        RegelResultatBehandlerResultat regelResultatBehandlerResultat = behandleRegelresultat(evaluering, aktuellPeriode, regelResultatBehandler,
                fastsettePeriodeGrunnlag.getArbeidsprosenter(), fastsettePeriodeGrunnlag);

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

    private void fastsettArbeidsprosenter(List<UttakPeriode> perioder, Arbeidsprosenter arbeidsprosenter) {
        for (UttakPeriode periode : perioder) {
            for (AktivitetIdentifikator aktivitet : arbeidsprosenter.getAktiviteter()) {
                BigDecimal arbeidsprosent = arbeidsprosenter.getArbeidsprosent(aktivitet, periode);
                periode.setArbeidsprosent(aktivitet, arbeidsprosent);
            }
        }
    }

    private RegelResultatBehandlerResultat behandleRegelresultat(Evaluation evaluering,
                                                                 UttakPeriode aktuellPeriode,
                                                                 RegelResultatBehandler behandler,
                                                                 Arbeidsprosenter arbeidsprosenter,
                                                                 FastsettePeriodeGrunnlag grunnlag) {
        final RegelResultatBehandlerResultat regelResultatBehandlerResultat;
        Regelresultat regelresultat = new Regelresultat(evaluering);
        aktuellPeriode.setSluttpunktTrekkerDager(regelresultat.trekkDagerFraSaldo());
        UtfallType utfallType = regelresultat.getUtfallType();

        UtfallType graderingUtfall = regelresultat.getGradering();
        boolean avslåGradering = UtfallType.AVSLÅTT.equals(graderingUtfall);
        GraderingIkkeInnvilgetÅrsak graderingIkkeInnvilgetÅrsak = regelresultat.getGraderingIkkeInnvilgetÅrsak();
        Optional<TomKontoKnekkpunkt> knekkpunktOpt = finnKnekkpunkt(aktuellPeriode, grunnlag);


        switch (utfallType) {
            case AVSLÅTT:
                regelResultatBehandlerResultat = behandler.avslåAktuellPeriode(aktuellPeriode, knekkpunktOpt, regelresultat.getAvklaringÅrsak(),
                        arbeidsprosenter, regelresultat.skalUtbetale(), overlapperMedInnvilgetAnnenpartsPeriode(aktuellPeriode, grunnlag.getAnnenPartUttaksperioder()));
                break;
            case INNVILGET:
                regelResultatBehandlerResultat = behandler.innvilgAktuellPeriode(aktuellPeriode, knekkpunktOpt, regelresultat.getInnvilgetÅrsak(), avslåGradering,
                        graderingIkkeInnvilgetÅrsak, arbeidsprosenter, regelresultat.skalUtbetale());
                break;
            case MANUELL_BEHANDLING:
                regelResultatBehandlerResultat = behandler.manuellBehandling(aktuellPeriode, regelresultat.getManuellbehandlingårsak(), regelresultat.getAvklaringÅrsak(),
                        arbeidsprosenter, regelresultat.skalUtbetale());
                break;
            default:
                throw new UnsupportedOperationException(String.format("Ukjent utfalltype: %s", utfallType.name()));
        }

        return regelResultatBehandlerResultat;
    }

    private boolean overlapperMedInnvilgetAnnenpartsPeriode(UttakPeriode aktuellPeriode, List<FastsattPeriodeAnnenPart> annenPartUttaksperioder) {
        return annenPartUttaksperioder.stream().anyMatch(annenpartsPeriode -> annenpartsPeriode.overlapper(aktuellPeriode) && annenpartsPeriode.isInnvilget());
    }

    private Optional<TomKontoKnekkpunkt> finnKnekkpunkt(UttakPeriode aktuellPeriode, FastsettePeriodeGrunnlag grunnlag) {
        if (aktuellPeriode instanceof UtsettelsePeriode || erFPFF(aktuellPeriode)) {
            return Optional.empty();
        }
        return TomKontoIdentifiserer.identifiser(grunnlag.getAktuellPeriode(), grunnlag.getAktiviteter(),
                grunnlag.getTrekkdagertilstand(), grunnlag.getStønadskontotype());
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
