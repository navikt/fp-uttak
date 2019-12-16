package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ValgAvStønadskontoTjeneste.velgStønadskonto;

import java.math.BigDecimal;
import java.util.Optional;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.saldo.SaldoUtregning;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;

public class RegelResultatBehandlerImpl implements RegelResultatBehandler {

    private final SaldoUtregning saldoUtregning;
    private final RegelGrunnlag regelGrunnlag;
    private final Konfigurasjon konfigurasjon;

    public RegelResultatBehandlerImpl(SaldoUtregning saldoUtregning, RegelGrunnlag regelGrunnlag, Konfigurasjon konfigurasjon) {
        this.saldoUtregning = saldoUtregning;
        this.regelGrunnlag = regelGrunnlag;
        this.konfigurasjon = konfigurasjon;
    }

    @Override
    public RegelResultatBehandlerResultat innvilgAktuellPeriode(UttakPeriode uttakPeriode,
                                                                Optional<TomKontoKnekkpunkt> knekkpunktOpt,
                                                                Årsak innvilgetÅrsak,
                                                                boolean avslåGradering,
                                                                GraderingIkkeInnvilgetÅrsak graderingIkkeInnvilgetÅrsak,
                                                                boolean utbetal) {
        final RegelResultatBehandlerResultat resultat = finnResultatInnvilget(uttakPeriode, knekkpunktOpt);
        resultat.getPeriode().setPerioderesultattype(Perioderesultattype.INNVILGET);
        resultat.getPeriode().setÅrsak(innvilgetÅrsak);
        if (avslåGradering) {
            resultat.getPeriode().opphevGradering(graderingIkkeInnvilgetÅrsak);
        }
        oppdaterUtbetalingsgrad(resultat.getPeriode(), utbetal, false);
        return resultat;
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
        oppdaterUtbetalingsgrad(resultat, utbetal, overlapperInnvilgetAnnenpartsPeriode);

        return regelResultatBehandlerResultat;
    }

    @Override
    public RegelResultatBehandlerResultat manuellBehandling(UttakPeriode uttakPeriode,
                                                            Manuellbehandlingårsak manuellbehandlingårsak,
                                                            Årsak ikkeOppfyltÅrsak,
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

        oppdaterUtbetalingsgrad(resultat, utbetal, false);

        return RegelResultatBehandlerResultat.utenKnekk(resultat);
    }

    private void validerKnekkpunkt(UttakPeriode uttakPeriode, TomKontoKnekkpunkt knekkpunkt) {
        if (!uttakPeriode.overlapper(knekkpunkt.getDato())) {
            throw new IllegalArgumentException("Knekkpunkt må være i periode. " + knekkpunkt.getDato() + " - " + uttakPeriode);
        }
    }

    private UtbetalingsprosentUtregning bestemUtregning(UttakPeriode uttakPeriode,
                                                        AktivitetIdentifikator aktivitet) {
        if (uttakPeriode.søktGradering(aktivitet)) {
            return new UtbetalingsprosentMedGraderingUtregning(uttakPeriode, aktivitet);
        } else if (uttakPeriode.getSamtidigUttak().isPresent()) {
            return new UtbetalingsprosentSamtidigUttakUtregning(uttakPeriode.getSamtidigUttak().get(), uttakPeriode.getGradertArbeidsprosent());
        }
        return new UtbetalingsprosentUtenGraderingUtregning();
    }

    private void oppdaterUtbetalingsgrad(UttakPeriode uttakPeriode, boolean utbetal, boolean overlapperInnvilgetAnnenpartsPeriode) {
        for (AktivitetIdentifikator aktivitet : uttakPeriode.getAktiviteter()) {
            BigDecimal utbetalingsgrad = BigDecimal.ZERO;
            if (overlapperInnvilgetAnnenpartsPeriode) {
                uttakPeriode.setSluttpunktTrekkerDager(aktivitet, false);
            } else if (saldoUtregning.saldoITrekkdager(uttakPeriode.getStønadskontotype(), aktivitet).merEnn0() ||
                    !(uttakPeriode instanceof StønadsPeriode) ||
                    uttakPeriode instanceof ManglendeSøktPeriode ||
                    Perioderesultattype.MANUELL_BEHANDLING.equals(uttakPeriode.getPerioderesultattype())) {
                if (utbetal) {
                    UtbetalingsprosentUtregning utregning = bestemUtregning(uttakPeriode, aktivitet);
                    utbetalingsgrad = utregning.resultat();
                }
            } else {
                uttakPeriode.setSluttpunktTrekkerDager(aktivitet, false);
            }
            uttakPeriode.setUtbetalingsgrad(aktivitet, utbetalingsgrad);
        }
        if (uttakPeriode.getSkalTrekkedager()) {
            if (Stønadskontotype.UKJENT.equals(uttakPeriode.getStønadskontotype())) {
                utledeKonto(uttakPeriode);
            }
        }
    }

    private void utledeKonto(UttakPeriode periode) {
        Optional<Stønadskontotype> stønadskontotypeOpt = velgStønadskonto(periode, regelGrunnlag, saldoUtregning, konfigurasjon);
        if (stønadskontotypeOpt.isPresent()) {
            periode.setStønadskontotype(stønadskontotypeOpt.get());
            //Går til manuell så saksbehandler kan rydde opp
        } else if (periode.getManuellbehandlingårsak() == null) {
            throw new IllegalStateException("Prøver å trekke dager fra ukjent konto. Periode " + periode.getFom() + " - " + periode.getTom());
        }
    }
}
