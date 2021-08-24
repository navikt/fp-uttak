package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmBareFarHarRett;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmBehandlingKreverSammenhengendeUttak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmDagerIgjenPåAlleAktiviteter;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodeErForeldrepengerFørFødsel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenInnenforUkerReservertMor;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmTomForAlleSineKontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfylt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Delregel innenfor regeltjenesten FastsettePeriodeRegel som fastsetter uttak av foreldrepenger før fødsel.
 * <p>
 * Utfall definisjoner:<br>
 * <p>
 * Utfall AVSLÅTT:<br>
 * - Far søker om perioden
 * - Perioden starter før perioden forbeholdt mor før fødsel.<br>
 * - Perioden starter etter termin/fødsel.<br>
 * <p>
 * Utfall INNVILGET:<br>
 * - Perioden dekker perioden forbeholdt mor før fødsel og det er mor som søker.
 */

@RuleDocumentation(value = ManglendeSøktPeriodeDelregel.ID, specificationReference = "https://confluence.adeo.no/display/MODNAV/1.+Samleside+for+oppdaterte+regelflyter")
public class ManglendeSøktPeriodeDelregel implements RuleService<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 10.7.FRI";

    private Konfigurasjon konfigurasjon;

    public ManglendeSøktPeriodeDelregel() {
        // For dokumentasjonsgenerering
    }


    ManglendeSøktPeriodeDelregel(Konfigurasjon konfigurasjon) {
        this.konfigurasjon = konfigurasjon;
    }

    @Override
    public Specification<FastsettePeriodeGrunnlag> getSpecification() {
        return sjekkOmForeldrepengerFørFødsel(new Ruleset<>());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmForeldrepengerFørFødsel(Ruleset<FastsettePeriodeGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmPeriodeErForeldrepengerFørFødsel.ID, "Er det Foreldrepenger før fødsel?")
                .hvis(new SjekkOmPeriodeErForeldrepengerFørFødsel(),
                        IkkeOppfylt.opprett("UT1073", IkkeOppfyltÅrsak.MOR_TAR_IKKE_UKENE_FØR_FØDSEL, true, false))
                .ellers(sjekkOmTomPåAlleSineKonto(rs));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmTomPåAlleSineKonto(Ruleset<FastsettePeriodeGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmTomForAlleSineKontoer.ID, SjekkOmTomForAlleSineKontoer.BESKRIVELSE)
                .hvis(new SjekkOmTomForAlleSineKontoer(),
                        IkkeOppfylt.opprett("UT1088", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, false, false))
                .ellers(sjekkOmDagerIgjenPåAlleAktiviteter(rs));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmDagerIgjenPåAlleAktiviteter(Ruleset<FastsettePeriodeGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmDagerIgjenPåAlleAktiviteter.ID, SjekkOmDagerIgjenPåAlleAktiviteter.BESKRIVELSE)
                .hvis(new SjekkOmDagerIgjenPåAlleAktiviteter(), sjekkOmBrukSammenhengendeUttakÅrsaker(rs))
                .ellers(Manuellbehandling.opprett("UT1291", null,
                        Manuellbehandlingårsak.STØNADSKONTO_TOM, false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmBrukSammenhengendeUttakÅrsaker(Ruleset<FastsettePeriodeGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmBehandlingKreverSammenhengendeUttak.ID, SjekkOmBehandlingKreverSammenhengendeUttak.BESKRIVELSE)
                .hvis(new SjekkOmBehandlingKreverSammenhengendeUttak(),
                        IkkeOppfylt.opprett("UT1087", IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER, true, false))
                .ellers(sjekkOmSakGjelderBareFarRett(rs));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmSakGjelderBareFarRett(Ruleset<FastsettePeriodeGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmBareFarHarRett.ID, SjekkOmBareFarHarRett.BESKRIVELSE)
                .hvis(new SjekkOmBareFarHarRett(),
                        IkkeOppfylt.opprett("UT1093", IkkeOppfyltÅrsak.BARE_FAR_RETT_IKKE_SØKT, true, false))
                .ellers(sjekkOmPeriodeGjelderMorsReserverteUker(rs));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeGjelderMorsReserverteUker(Ruleset<FastsettePeriodeGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmPeriodenInnenforUkerReservertMor.ID, "Innenfor mors reserverte uker")
                .hvis(new SjekkOmPeriodenInnenforUkerReservertMor(konfigurasjon),
                        IkkeOppfylt.opprett("UT1094", IkkeOppfyltÅrsak.MOR_TAR_IKKE_UKENE_ETTER_FØDSEL, true, false))
                .ellers(Manuellbehandling.opprett("UT1095", null,
                        Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, true, false));
    }

}
