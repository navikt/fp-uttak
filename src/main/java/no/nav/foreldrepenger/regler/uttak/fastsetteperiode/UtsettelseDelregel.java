package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmBareFarHarRett;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmBarnInnlagt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmFødselErFørUke33;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodeErFørTermin;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenStarterFørFamiliehendelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSykdomSkade;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøkerInnlagt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøknadGjelderTerminEllerFødsel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmTidsperiodeForbeholdtMor;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUtsettelsePgaBarnetsInnleggelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUtsettelsePgaSykdomSkade;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUtsettelsePgaSøkerInnleggelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav.SjekkOmMorErIAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.FastsettePeriodeUtfall;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfylt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Oppfylt;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.specification.Specification;

@RuleDocumentation(value = UtsettelseDelregel.ID)
public class UtsettelseDelregel implements RuleService<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 18";
    private final Ruleset<FastsettePeriodeGrunnlag> rs = new Ruleset<>();
    private Konfigurasjon konfigurasjon;

    public UtsettelseDelregel(Konfigurasjon konfigurasjon) {
        this.konfigurasjon = konfigurasjon;
    }

    public UtsettelseDelregel() {
        // For regeldokumentasjon
    }

    @Override
    public Specification<FastsettePeriodeGrunnlag> getSpecification() {
        return rs.hvisRegel(SjekkOmSøknadGjelderTerminEllerFødsel.ID, SjekkOmSøknadGjelderTerminEllerFødsel.BESKRIVELSE)
                .hvis(new SjekkOmSøknadGjelderTerminEllerFødsel(), sjekkOmTidsperiodeForbeholdtMor())
                .ellers(sjekkOmPeriodeErFørFamiliehendelseVedAdopsjon());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodeErFørFamiliehendelseVedAdopsjon() {
        return rs.hvisRegel(SjekkOmPeriodenStarterFørFamiliehendelse.ID, SjekkOmPeriodenStarterFørFamiliehendelse.BESKRIVELSE)
                .hvis(new SjekkOmPeriodenStarterFørFamiliehendelse(), Manuellbehandling.opprett("UT1350",
                        IkkeOppfyltÅrsak.UTSETTELSE_FØR_TERMIN_FØDSEL, Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE, false, false))
                .ellers(sjekkOmBareFarHarRett());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmBareFarHarRett() {
        return rs.hvisRegel(SjekkOmBareFarHarRett.ID, SjekkOmBareFarHarRett.BESKRIVELSE)
                .hvis(new SjekkOmBareFarHarRett(), sjekkOmMorErIAktivitet())
                .ellers(Oppfylt.opprett("UT1351", InnvilgetÅrsak.UTSETTELSE_GYLDIG, false, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmMorErIAktivitet() {
        return rs.hvisRegel(SjekkOmMorErIAktivitet.ID, SjekkOmMorErIAktivitet.BESKRIVELSE)
                .hvis(new SjekkOmMorErIAktivitet(), Oppfylt.opprett("UT1352", InnvilgetÅrsak.UTSETTELSE_GYLDIG_BFR_AKT_KRAV_OPPFYLT, false, false))
                .ellers(new AvslagAktivitetskravDelregel().getSpecification());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmTidsperiodeForbeholdtMor() {
        return rs.hvisRegel(SjekkOmTidsperiodeForbeholdtMor.ID, SjekkOmTidsperiodeForbeholdtMor.BESKRIVELSE)
                .hvis(new SjekkOmTidsperiodeForbeholdtMor(konfigurasjon), sjekkOmSykdomSkade())
                .ellers(sjekkOmBareFarHarRett());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmSykdomSkade() {
        var erSøkerSykErDokumentert =  rs.hvisRegel(SjekkOmSykdomSkade.ID, SjekkOmSykdomSkade.BESKRIVELSE)
                .hvis(new SjekkOmSykdomSkade(), Oppfylt.opprett("UT1353", InnvilgetÅrsak.UTSETTELSE_GYLDIG_SEKS_UKER_FRI_SYKDOM, false, false))
                .ellers(Manuellbehandling.opprett("UT1354", IkkeOppfyltÅrsak.SØKERS_SYKDOM_SKADE_SEKS_UKER_IKKE_OPPFYLT,
                        Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE, true, false));
        return rs.hvisRegel(SjekkOmUtsettelsePgaSykdomSkade.ID, SjekkOmUtsettelsePgaSykdomSkade.BESKRIVELSE)
                .hvis(new SjekkOmUtsettelsePgaSykdomSkade(), erSøkerSykErDokumentert)
                .ellers(sjekkOmSøkersInnleggelse());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmSøkersInnleggelse() {
        var erSøkerInnlagtErDokumentert =  rs.hvisRegel(SjekkOmSøkerInnlagt.ID, SjekkOmSøkerInnlagt.BESKRIVELSE)
                .hvis(new SjekkOmSøkerInnlagt(), Oppfylt.opprett("UT1355", InnvilgetÅrsak.UTSETTELSE_GYLDIG_SEKS_UKER_INNLEGGELSE, false, false))
                .ellers(Manuellbehandling.opprett("UT1356", IkkeOppfyltÅrsak.SØKERS_INNLEGGELSE_SEKS_UKER_IKKE_OPPFYLT,
                        Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE, true, false));
        return rs.hvisRegel(SjekkOmUtsettelsePgaSøkerInnleggelse.ID, SjekkOmUtsettelsePgaSøkerInnleggelse.BESKRIVELSE)
                .hvis(new SjekkOmUtsettelsePgaSøkerInnleggelse(), erSøkerInnlagtErDokumentert)
                .ellers(sjekkOmBarnetsInnleggelse());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmBarnetsInnleggelse() {
        return rs.hvisRegel(SjekkOmUtsettelsePgaBarnetsInnleggelse.ID, SjekkOmUtsettelsePgaBarnetsInnleggelse.BESKRIVELSE)
                .hvis(new SjekkOmUtsettelsePgaBarnetsInnleggelse(), sjekkOmBarnetVarInnlagt())
                .ellers(Manuellbehandling.opprett("UT1357", IkkeOppfyltÅrsak.UTSETTELSE_INNENFOR_DE_FØRSTE_6_UKENE,
                        Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmBarnetVarInnlagt() {
        return rs.hvisRegel(SjekkOmBarnInnlagt.ID, SjekkOmBarnInnlagt.BESKRIVELSE)
                .hvis(new SjekkOmBarnInnlagt(), sjekkOmFødselFørUke33())
                .ellers(Manuellbehandling.opprett("UT1358", IkkeOppfyltÅrsak.BARNETS_INNLEGGELSE_SEKS_UKER_IKKE_OPPFYLT,
                        Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmFødselFørUke33() {
        return rs.hvisRegel(SjekkOmFødselErFørUke33.ID, SjekkOmFødselErFørUke33.BESKRIVELSE)
                .hvis(new SjekkOmFødselErFørUke33(konfigurasjon), sjekkOmPeriodenErFørTermin())
                .ellers(innvilgUT1359());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmPeriodenErFørTermin() {
        return rs.hvisRegel(SjekkOmPeriodeErFørTermin.ID, SjekkOmPeriodeErFørTermin.BESKRIVELSE)
                //prematuruker
                .hvis(new SjekkOmPeriodeErFørTermin(), IkkeOppfylt.opprett("UT1360", IkkeOppfyltÅrsak.FRATREKK_PLEIEPENGER,
                        true, false))
                .ellers(innvilgUT1359());
    }

    private FastsettePeriodeUtfall innvilgUT1359() {
        return Oppfylt.opprett("UT1359", InnvilgetÅrsak.UTSETTELSE_GYLDIG_SEKS_UKER_FRI_BARN_INNLAGT, false, false);
    }
}
