package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.regelflyt;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmOverføringPgaAleneomsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmOverføringPgaInnleggelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmOverføringPgaSykdomSkade;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Oppfylt;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Delregel innenfor regeltjenesten FastsettePeriodeRegel som fastsetter uttakperioder som inneholder delregelen for
 * innvilgelse av overføring av mødrekvote og fedrekvote.
 *
 * <p>Utfall definisjoner:<br>
 *
 * <p>Utfall INNVILGET:<br>
 * - Perioden er avklart at har gyldig grunn for overføring.<br>
 *
 * <p>
 */
public class OverføringDelregel implements RuleService<FastsettePeriodeGrunnlag> {

    private final Ruleset<FastsettePeriodeGrunnlag> rs = new Ruleset<>();

    public OverføringDelregel() {
        // For regeldokumentasjon
    }

    @Override
    public Specification<FastsettePeriodeGrunnlag> getSpecification() {
        return rs.hvisRegel(
                        SjekkOmOverføringPgaInnleggelse.ID,
                        "Er det søkt om overføring som følge av innleggelse på institusjon?")
                .hvis(
                        new SjekkOmOverføringPgaInnleggelse(),
                        Oppfylt.opprett("UT1173", InnvilgetÅrsak.OVERFØRING_ANNEN_PART_INNLAGT, true))
                .ellers(sjekkOmOverføringPgaSykdomSkade());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmOverføringPgaSykdomSkade() {
        return rs.hvisRegel(SjekkOmOverføringPgaSykdomSkade.ID, "Er det søkt om overføring som følge av sykdom/skade?")
                .hvis(
                        new SjekkOmOverføringPgaSykdomSkade(),
                        Oppfylt.opprett("UT1172", InnvilgetÅrsak.OVERFØRING_ANNEN_PART_SYKDOM_SKADE, true))
                .ellers(sjekkOmOverføringPgaAleneomsorgEllerIkkeRett());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmOverføringPgaAleneomsorgEllerIkkeRett() {
        return rs.hvisRegel(
                        SjekkOmOverføringPgaAleneomsorg.ID,
                        "Er det søkt om overføring som følge av aleneomsorg eller annen forelder ikke har rett?")
                .hvis(
                        new SjekkOmOverføringPgaAleneomsorg(),
                        Oppfylt.opprett("UT1174", InnvilgetÅrsak.OVERFØRING_ALENEOMSORG, true))
                .ellers(Oppfylt.opprett("UT1175", InnvilgetÅrsak.OVERFØRING_ANNEN_PART_IKKE_RETT, true));
    }
}
