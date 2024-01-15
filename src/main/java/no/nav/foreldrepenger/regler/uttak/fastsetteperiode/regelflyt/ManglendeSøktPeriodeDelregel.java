package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.regelflyt;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmBareFarHarRett;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmBehandlingKreverSammenhengendeUttak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmDagerIgjenPåAlleAktiviteter;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmErAlleDisponibleDagerIgjenMinsterett;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodeErForeldrepengerFørFødsel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenInnenforUkerReservertMor;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenOpprettetAvFødselshendelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmTomForAlleSineKontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.FastsettePeriodeUtfall;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfylt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Oppfylt;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Delregel innenfor regeltjenesten FastsettePeriodeRegel som fastsetter perioder som ikke er søkt om.
 * Disse er i hovedsak innenfor perioder som er reservert mor (14-9 sjette ledd) og tekniske trekkperioder for far (14-14 første ledd)
 */

@RuleDocumentation(value = ManglendeSøktPeriodeDelregel.ID, specificationReference = "https://confluence.adeo.no/display/MODNAV/1.+Samleside+for+oppdaterte+regelflyter")
public class ManglendeSøktPeriodeDelregel implements RuleService<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 10.7.FRI";

    ManglendeSøktPeriodeDelregel() {
        // For dokumentasjonsgenerering
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
                .hvis(new SjekkOmTomForAlleSineKontoer(), sjekkOmSakGjelderBareFarRettTomKonto(rs))
                .ellers(sjekkOmDagerIgjenPåAlleAktiviteter(rs));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmSakGjelderBareFarRettTomKonto(Ruleset<FastsettePeriodeGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmBareFarHarRett.ID, SjekkOmBareFarHarRett.BESKRIVELSE)
                .hvis(new SjekkOmBareFarHarRett(), sjekkOmDagerIgjenPåMinsterett(rs))
                .ellers(ut1088());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmDagerIgjenPåMinsterett(Ruleset<FastsettePeriodeGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmErAlleDisponibleDagerIgjenMinsterett.ID, SjekkOmErAlleDisponibleDagerIgjenMinsterett.BESKRIVELSE)
                .hvis(new SjekkOmErAlleDisponibleDagerIgjenMinsterett(), IkkeOppfylt.opprett("UT1089", IkkeOppfyltÅrsak.BARE_FAR_RETT_IKKE_SØKT, false, false))
                .ellers(ut1088());
    }

    private FastsettePeriodeUtfall ut1088() {
        return IkkeOppfylt.opprett("UT1088", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, false, false);
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmDagerIgjenPåAlleAktiviteter(Ruleset<FastsettePeriodeGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmDagerIgjenPåAlleAktiviteter.ID, SjekkOmDagerIgjenPåAlleAktiviteter.BESKRIVELSE)
                .hvis(new SjekkOmDagerIgjenPåAlleAktiviteter(), sjekkOmSakGjelderBareFarRett(rs))
                .ellers(Manuellbehandling.opprett("UT1291", null,
                        Manuellbehandlingårsak.STØNADSKONTO_TOM, false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmBrukSammenhengendeUttakÅrsaker(Ruleset<FastsettePeriodeGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmBehandlingKreverSammenhengendeUttak.ID, SjekkOmBehandlingKreverSammenhengendeUttak.BESKRIVELSE)
                .hvis(new SjekkOmBehandlingKreverSammenhengendeUttak(),
                    Manuellbehandling.opprett("UT1096", IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER, Manuellbehandlingårsak.VURDER_OM_UTSETTELSE, true, false))
                .ellers(sjekkOmPeriodeGjelderMorsReserverteUker(rs));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmSakGjelderBareFarRett(Ruleset<FastsettePeriodeGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmBareFarHarRett.ID, SjekkOmBareFarHarRett.BESKRIVELSE)
                .hvis(new SjekkOmBareFarHarRett(),
                        IkkeOppfylt.opprett("UT1093", IkkeOppfyltÅrsak.BARE_FAR_RETT_IKKE_SØKT, true, false))
                .ellers(sjekkOmBrukSammenhengendeUttakÅrsaker(rs));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeGjelderMorsReserverteUker(Ruleset<FastsettePeriodeGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmPeriodenInnenforUkerReservertMor.ID, "Innenfor mors reserverte uker")
                .hvis(new SjekkOmPeriodenInnenforUkerReservertMor(), sjekkOmPeriodeErOpprettetAvFødselshendelse(rs))
                .ellers(Manuellbehandling.opprett("UT1095", null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeErOpprettetAvFødselshendelse(Ruleset<FastsettePeriodeGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmPeriodenOpprettetAvFødselshendelse.ID, SjekkOmPeriodenOpprettetAvFødselshendelse.BESKRIVELSE)
                .hvis(new SjekkOmPeriodenOpprettetAvFødselshendelse(),
                        Oppfylt.opprett("UT1097", InnvilgetÅrsak.MSP_INNVILGET, true, true))
                .ellers(IkkeOppfylt.opprett("UT1094", IkkeOppfyltÅrsak.MOR_TAR_IKKE_UKENE_ETTER_FØDSEL, true, false));
    }

}
