package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.regelflyt;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmGradertPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmOmsorgHelePerioden;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenStarterFørFamiliehendelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfylt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.specification.Specification;

@RuleDocumentation(value = StebarnsadopsjonDelRegel.ID)
public class StebarnsadopsjonDelRegel implements RuleService<FastsettePeriodeGrunnlag> {


    public static final String ID = "FP_VK 16.1";

    private final Ruleset<FastsettePeriodeGrunnlag> rs = new Ruleset<>();

    public StebarnsadopsjonDelRegel() {
        // For regeldokumentasjon
    }

    @Override
    public Specification<FastsettePeriodeGrunnlag> getSpecification() {
        return rs.hvisRegel(SjekkOmPeriodenStarterFørFamiliehendelse.ID, "Starter perioden før omsorgsovertakelse?")
            .hvis(new SjekkOmPeriodenStarterFørFamiliehendelse(),
                IkkeOppfylt.opprett("UT1285", IkkeOppfyltÅrsak.FØR_OMSORGSOVERTAKELSE, false, false))
            .ellers(sjekkOmSøkerHarOmsorg());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmSøkerHarOmsorg() {
        return rs.hvisRegel(SjekkOmOmsorgHelePerioden.ID, "Har søker omsorg for barnet?")
            .hvis(new SjekkOmOmsorgHelePerioden(), sjekkOmNoenDisponibleDager())
            .ellers(IkkeOppfylt.opprett("UT1240", IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG, true, false));
    }


    private Specification<FastsettePeriodeGrunnlag> sjekkOmNoenDisponibleDager() {
        return rs.hvisRegel(SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto.ID, "Er det noen disponible stønadsdager på mødrekvote?")
            .hvis(new SjekkOmTilgjengeligeDagerPåNoenAktiviteteneForSøktStønadskonto(), sjekkOmGraderingIPerioden())
            .ellers(
                Manuellbehandling.opprett("UT1244", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, Manuellbehandlingårsak.STØNADSKONTO_TOM, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmGraderingIPerioden() {
        return rs.hvisRegel(SjekkOmGradertPeriode.ID, SjekkOmGradertPeriode.BESKRIVELSE)
            .hvis(new SjekkOmGradertPeriode(), Manuellbehandling.opprett("UT1242", null, Manuellbehandlingårsak.STEBARNSADOPSJON, true, false))
            .ellers(Manuellbehandling.opprett("UT1241", null, Manuellbehandlingårsak.STEBARNSADOPSJON, true, false));
    }
}
