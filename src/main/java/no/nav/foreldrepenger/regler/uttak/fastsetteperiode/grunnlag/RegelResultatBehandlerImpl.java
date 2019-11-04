package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ValgAvStønadskontoTjeneste.velgStønadskonto;

import java.math.BigDecimal;
import java.util.Optional;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;

public class RegelResultatBehandlerImpl implements RegelResultatBehandler {

    private final Trekkdagertilstand trekkdagertilstand;
    private final RegelGrunnlag regelGrunnlag;
    private final Konfigurasjon konfigurasjon;

    public RegelResultatBehandlerImpl(Trekkdagertilstand trekkdagertilstand, RegelGrunnlag regelGrunnlag, Konfigurasjon konfigurasjon) {
        this.trekkdagertilstand = trekkdagertilstand;
        this.regelGrunnlag = regelGrunnlag;
        this.konfigurasjon = konfigurasjon;
    }

    @Override
    public RegelResultatBehandlerResultat innvilgAktuellPeriode(UttakPeriode uttakPeriode,
                                                                Optional<TomKontoKnekkpunkt> knekkpunktOpt,
                                                                Årsak innvilgetÅrsak,
                                                                boolean avslåGradering,
                                                                GraderingIkkeInnvilgetÅrsak graderingIkkeInnvilgetÅrsak,
                                                                Arbeidsprosenter arbeidsprosenter,
                                                                boolean utbetal) {
        final RegelResultatBehandlerResultat resultat = finnResultatInnvilget(uttakPeriode, knekkpunktOpt);
        oppdaterResultat(avslåGradering, graderingIkkeInnvilgetÅrsak, arbeidsprosenter, resultat.getPeriode(), innvilgetÅrsak, utbetal);
        trekkSaldo(resultat.getPeriode());
        return resultat;
    }

    private void oppdaterResultat(boolean avslåGradering,
                                  GraderingIkkeInnvilgetÅrsak graderingIkkeInnvilgetÅrsak,
                                  Arbeidsprosenter arbeidsprosenter,
                                  UttakPeriode uttakPeriode,
                                  Årsak innvilgetÅrsak,
                                  boolean utbetal) {
        uttakPeriode.setPerioderesultattype(Perioderesultattype.INNVILGET);
        uttakPeriode.setÅrsak(innvilgetÅrsak);
        if (avslåGradering) {
            uttakPeriode.opphevGradering(graderingIkkeInnvilgetÅrsak);
        }
        oppdaterUtbetalingsgrad(uttakPeriode, arbeidsprosenter, utbetal);

    }

    private RegelResultatBehandlerResultat finnResultatInnvilget(UttakPeriode uttakPeriode, Optional<TomKontoKnekkpunkt> knekkpunktOpt) {
        if (knekkpunktOpt.isEmpty()) {
            return RegelResultatBehandlerResultat.utenKnekk(uttakPeriode.kopiMedNyPeriode(uttakPeriode.getFom(), uttakPeriode.getTom()));
        }

        validerKnekkpunkt(uttakPeriode, knekkpunktOpt.get());
        return finnResultatInnvilgetVedKnekk(uttakPeriode, knekkpunktOpt.get());
    }

    private RegelResultatBehandlerResultat finnResultatInnvilgetVedKnekk(UttakPeriode uttakPeriode, TomKontoKnekkpunkt knekkpunkt) {
        UttakPeriode førKnekk = uttakPeriode.kopiMedNyPeriode(uttakPeriode.getFom(), knekkpunkt.getDato().minusDays(1));
        UttakPeriode etterKnekk = uttakPeriode.kopiMedNyPeriode(knekkpunkt.getDato(), uttakPeriode.getTom());
        return RegelResultatBehandlerResultat.medKnekk(førKnekk, etterKnekk);
    }

    @Override
    public RegelResultatBehandlerResultat avslåAktuellPeriode(UttakPeriode uttakPeriode,
                                                              Optional<TomKontoKnekkpunkt> knekkpunktOpt,
                                                              Årsak årsak,
                                                              Arbeidsprosenter arbeidsprosenter,
                                                              boolean utbetal,
                                                              boolean overlapperInnvilgetAnnenpartsPeriode) {
        final RegelResultatBehandlerResultat regelResultatBehandlerResultat;
        UttakPeriode resultat;
        if (!overlapperInnvilgetAnnenpartsPeriode && knekkpunktOpt.isPresent()) {
            validerKnekkpunkt(uttakPeriode, knekkpunktOpt.get());
            resultat = uttakPeriode.kopiMedNyPeriode(uttakPeriode.getFom(), knekkpunktOpt.get().getDato().minusDays(1));
            resultat.setPerioderesultattype(Perioderesultattype.AVSLÅTT);
            resultat.setÅrsak(årsak);
            UttakPeriode etterKnekk = uttakPeriode.kopiMedNyPeriode(knekkpunktOpt.get().getDato(), uttakPeriode.getTom());
            regelResultatBehandlerResultat = RegelResultatBehandlerResultat.medKnekk(resultat, etterKnekk);
        } else {
            resultat = uttakPeriode.kopiMedNyPeriode(uttakPeriode.getFom(), uttakPeriode.getTom());
            resultat.setPerioderesultattype(Perioderesultattype.AVSLÅTT);
            resultat.setÅrsak(årsak);
            regelResultatBehandlerResultat = RegelResultatBehandlerResultat.utenKnekk(resultat);
        }
        oppdaterUtbetalingsgrad(resultat, arbeidsprosenter, utbetal);
        if (overlapperInnvilgetAnnenpartsPeriode) {
            resultat.overstyrSluttpunktOmSluttpunktSkalTrekkedager();
        }
        trekkSaldo(resultat);

        return regelResultatBehandlerResultat;
    }

    @Override
    public RegelResultatBehandlerResultat manuellBehandling(UttakPeriode uttakPeriode,
                                                            Manuellbehandlingårsak manuellbehandlingårsak,
                                                            Årsak ikkeOppfyltÅrsak,
                                                            Arbeidsprosenter arbeidsprosenter,
                                                            boolean utbetal,
                                                            boolean avslåGradering,
                                                            GraderingIkkeInnvilgetÅrsak graderingIkkeInnvilgetÅrsak) {
        UttakPeriode resultat = uttakPeriode.kopiMedNyPeriode(uttakPeriode.getFom(), uttakPeriode.getTom());
        resultat.setPerioderesultattype(Perioderesultattype.MANUELL_BEHANDLING);
        resultat.setManuellbehandlingårsak(manuellbehandlingårsak);
        resultat.setÅrsak(ikkeOppfyltÅrsak);

        if (avslåGradering) {
            resultat.opphevGradering(graderingIkkeInnvilgetÅrsak);
        }

        oppdaterUtbetalingsgrad(resultat, arbeidsprosenter, utbetal);
        trekkSaldo(resultat);

        return RegelResultatBehandlerResultat.utenKnekk(resultat);
    }

    private void validerKnekkpunkt(UttakPeriode uttakPeriode, TomKontoKnekkpunkt knekkpunkt) {
        if (!uttakPeriode.overlapper(knekkpunkt.getDato())) {
            throw new IllegalArgumentException("Knekkpunkt må være i periode. " + knekkpunkt.getDato() + " - " + uttakPeriode);
        }
    }

    private void oppdaterUtbetalingsgrad(UttakPeriode uttakPeriode, Arbeidsprosenter arbeidsprosenter, boolean utbetal) {
        for (AktivitetIdentifikator aktivitet : arbeidsprosenter.getAktiviteter()) {
            final BigDecimal utbetalingsgrad;
            if (utbetal) {
                UtbetalingsprosentUtregning utregning = bestemUtregning(uttakPeriode, aktivitet, arbeidsprosenter);
                utbetalingsgrad = utregning.resultat();
            } else {
                utbetalingsgrad = BigDecimal.ZERO;
            }
            uttakPeriode.setUtbetalingsgrad(aktivitet, utbetalingsgrad);
        }
    }

    private UtbetalingsprosentUtregning bestemUtregning(UttakPeriode uttakPeriode,
                                                        AktivitetIdentifikator aktivitet,
                                                        Arbeidsprosenter arbeidsprosenter) {
        if (uttakPeriode.søktGradering(aktivitet)) {
            return new UtbetalingsprosentMedGraderingUtregning(arbeidsprosenter, aktivitet, uttakPeriode);
        } else if (uttakPeriode.getSamtidigUttak().isPresent()){
            return new UtbetalingsprosentSamtidigUttakUtregning(uttakPeriode.getSamtidigUttak().get(), uttakPeriode.getGradertArbeidsprosent());
        }
        return new UtbetalingsprosentUtenGraderingUtregning(arbeidsprosenter, aktivitet, uttakPeriode);
    }

    private void trekkSaldo(UttakPeriode uttakPeriode) {
        if (uttakPeriode.getSluttpunktTrekkerDager()) {
            if (Stønadskontotype.UKJENT.equals(uttakPeriode.getStønadskontotype())) {
                utledeKonto(uttakPeriode);
            }
            trekkdagertilstand.reduserSaldo(uttakPeriode);
        }
    }

    private void utledeKonto(UttakPeriode periode) {
        Optional<Stønadskontotype> stønadskontotypeOpt = velgStønadskonto(periode, regelGrunnlag, trekkdagertilstand, konfigurasjon);
        if (stønadskontotypeOpt.isPresent()) {
            periode.setStønadskontotype(stønadskontotypeOpt.get());
            //Går til manuell så saksbehandler kan rydde opp
        } else if (periode.getManuellbehandlingårsak() == null) {
            throw new IllegalStateException("Prøver å trekke dager fra ukjent konto. Periode " + periode.getFom()  + " - " + periode.getTom());
        }
    }
}
