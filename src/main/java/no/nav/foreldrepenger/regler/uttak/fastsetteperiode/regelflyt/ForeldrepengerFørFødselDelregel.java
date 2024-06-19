package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.regelflyt;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmForeldrepengerFørFødselStarterForTidligEllerSlutterForSent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmGradertPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøkerErMor;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.GraderingIkkeInnvilgetÅrsak;
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
 * Delregel innenfor regeltjenesten FastsettePeriodeRegel som fastsetter uttak av foreldrepenger før
 * fødsel.
 *
 * <p>Utfall definisjoner:<br>
 *
 * <p>Utfall AVSLÅTT:<br>
 * - Far søker om perioden - Perioden starter før perioden forbeholdt mor før fødsel.<br>
 * - Perioden starter etter termin/fødsel.<br>
 *
 * <p>Utfall INNVILGET:<br>
 * - Perioden dekker perioden forbeholdt mor før fødsel og det er mor som søker.
 */
@RuleDocumentation(
        value = ForeldrepengerFørFødselDelregel.ID,
        specificationReference =
                "https://confluence.adeo.no/display/MODNAV/1.+Samleside+for+oppdaterte+regelflyter")
public class ForeldrepengerFørFødselDelregel implements RuleService<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK XX10";

    ForeldrepengerFørFødselDelregel() {
        // For dokumentasjonsgenerering
    }

    @Override
    public Specification<FastsettePeriodeGrunnlag> getSpecification() {
        return sjekkOmSøkerErMorNode(new Ruleset<>());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmSøkerErMorNode(
            Ruleset<FastsettePeriodeGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmSøkerErMor.ID, SjekkOmSøkerErMor.BESKRIVELSE)
                .hvis(new SjekkOmSøkerErMor(), sjekkOmPeriodenStarterForTidligNode(rs))
                .ellers(
                        Manuellbehandling.opprett(
                                "UT1076",
                                IkkeOppfyltÅrsak.FAR_PERIODE_FØR_FØDSEL,
                                Manuellbehandlingårsak.FAR_SØKER_FØR_FØDSEL,
                                false,
                                false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodenStarterForTidligNode(
            Ruleset<FastsettePeriodeGrunnlag> rs) {
        return rs.hvisRegel(
                        SjekkOmForeldrepengerFørFødselStarterForTidligEllerSlutterForSent.ID,
                        "Starter perioden for tidlig?")
                .hvis(
                        new SjekkOmForeldrepengerFørFødselStarterForTidligEllerSlutterForSent(),
                        Manuellbehandling.opprett(
                                "UT1070",
                                null,
                                Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO,
                                true,
                                false))
                .ellers(sjekkOmGradering(rs));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmGradering(
            Ruleset<FastsettePeriodeGrunnlag> rs) {
        return rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
                .hvis(
                        new SjekkOmGradertPeriode(),
                        Oppfylt.opprettMedAvslåttGradering(
                                "UT1072",
                                InnvilgetÅrsak.FORELDREPENGER_FØR_FØDSEL,
                                GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_FOR_TIDLIG_GRADERING,
                                true))
                .ellers(Oppfylt.opprett("UT1071", InnvilgetÅrsak.FORELDREPENGER_FØR_FØDSEL, true));
    }
}
