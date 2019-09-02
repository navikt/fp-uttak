package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.Optional;

/**
 * Dette interfacet er beregnet til å brukes i orkestrerer for å innvilge/avslå perioder, og for
 * å flytte aktuell periode til neste periode.
 */
public interface RegelResultatBehandler {

    /**
     * Hvis knekkpunkt er gitt, knekk opp aktuell periode og innvilg første del av perioden.
     * Hvis knekkpunkt er null, innvilg hele perioden.
     * Sett aktuell periode til neste periode.
     * @param knekkpunktOpt Knekkpunkt når den avknekte perioden skal starte.
     * @param innvilgetÅrsak
     * @param avslåGradering avlå gradering på perioden dersom det er søkt om gradering.
     * @param graderingIkkeInnvilgetÅrsak årsak til at graderinger ikke ble innvilget. Null dersom gradering ble innvilget.
     * @param arbeidsprosenter arbeidsprosenter som skal brukes for å beregne utbetalingsgrad.
     * @param utbetal true dersom perioden skal utbetales, false dersom perioden ikke skal utbetales.
     */
    RegelResultatBehandlerResultat innvilgAktuellPeriode(UttakPeriode uttaksperiode,
                                                         Optional<TomKontoKnekkpunkt> knekkpunktOpt,
                                                         Årsak innvilgetÅrsak,
                                                         boolean avslåGradering,
                                                         GraderingIkkeInnvilgetÅrsak graderingIkkeInnvilgetÅrsak,
                                                         Arbeidsprosenter arbeidsprosenter,
                                                         boolean utbetal);

    /**
     * Hvis knekkpunkt er gitt, knekk opp aktuell periode og avslå første del av perioden.
     * Hvis knekkpunkt er null, avslå hele perioden.
     * Sett aktuell periode til neste periode.
     *
     * @param knekkpunktOpt Knekkpunkt når den avknekte perioden skal starte.
     * @param årsak grunn til at perioden ikke ble oppfylt.
     * @param arbeidsprosenter arbeidsprosenter som skal brukes for å beregne utbetalingsgrad.
     * @param utbetal true dersom perioden skal utbetales, false dersom perioden ikke skal utbetales.
     * @param utbetal true dersom perioden overlapper med en periode hos annenpart.
     */
    RegelResultatBehandlerResultat avslåAktuellPeriode(UttakPeriode uttakPeriode,
                                                       Optional<TomKontoKnekkpunkt> knekkpunktOpt,
                                                       Årsak årsak,
                                                       Arbeidsprosenter arbeidsprosenter,
                                                       boolean utbetal,
                                                       boolean overlapperMedAnnenpart);

    /**
     * Sett en periode til manuell behandling med en årsak.
     *
     * @param manuellbehandlingårsak årsak til at manuell behandling ble trigget.
     * @param ikkeOppfyltÅrsak foreslått årsak til at perioden ikke er oppfylt.
     * @param arbeidsprosenter arbeidsprosenter som skal brukes for å beregne utbetalingsgrad.
     * @param utbetal true dersom perioden skal utbetales, false dersom perioden ikke skal utbetales.
     */
    RegelResultatBehandlerResultat manuellBehandling(UttakPeriode uttakPeriode,
                                                     Manuellbehandlingårsak manuellbehandlingårsak,
                                                     Årsak ikkeOppfyltÅrsak,
                                                     Arbeidsprosenter arbeidsprosenter,
                                                     boolean utbetal);
}
