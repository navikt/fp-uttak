package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmBareFarHarRett;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmBarnInnlagt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmDokumentertHV;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmDokumentertTiltakViaNav;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmErUtsettelseFørSøknadMottattdato;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmFeriePåBevegeligHelligdag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmFulltArbeidForUtsettelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmFødselErFørUke33;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodeErFørTermin;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmPeriodenStarterFørFamiliehendelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSykdomSkade;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøkerErArbeidstaker;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøkerInnlagt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmSøknadGjelderTerminEllerFødsel;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmTomForAlleSineKontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUtsettelsePgaArbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUtsettelsePgaFerie;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUtsettelsePgaHV;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUtsettelsePgaSykdomSkade;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUtsettelsePgaSøkerInnleggelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUtsettelsePgaTiltakViaNav;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmUttakSkjerEtterDeFørsteUkene;
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
    private Konfigurasjon konfigurasjon;
    private Ruleset<FastsettePeriodeGrunnlag> rs = new Ruleset<>();

    public UtsettelseDelregel() {
        // For regeldokumentasjon
    }

    public UtsettelseDelregel(Konfigurasjon konfigurasjon) {
        this.konfigurasjon = konfigurasjon;
    }

    @Override
    public Specification<FastsettePeriodeGrunnlag> getSpecification() {
        return rs.hvisRegel(SjekkOmTomForAlleSineKontoer.ID, SjekkOmTomForAlleSineKontoer.BESKRIVELSE)
                .hvis(new SjekkOmTomForAlleSineKontoer(), IkkeOppfylt.opprett("UT1125", IkkeOppfyltÅrsak.INGEN_STØNADSDAGER_IGJEN_FOR_AVSLÅTT_UTSETTELSE, false, false))
                .ellers(sjekkOmFerie());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmFerie() {
        return rs.hvisRegel(SjekkOmUtsettelsePgaFerie.ID, "Er det utsettelse pga ferie?")
                .hvis(new SjekkOmUtsettelsePgaFerie(), delRegelForFerie())
                .ellers(sjekkOmUtsettelsePgaArbeid());
    }

    private Specification<FastsettePeriodeGrunnlag> delRegelForFerie() {
        Specification<FastsettePeriodeGrunnlag> sjekkOmBareFarHarRettNode = rs.hvisRegel(SjekkOmBareFarHarRett.ID, SjekkOmBareFarHarRett.BESKRIVELSE)
            .hvis(new SjekkOmBareFarHarRett(), Manuellbehandling.opprett("UT1106", null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT, true, false))
            .ellers(Oppfylt.opprett("UT1108", InnvilgetÅrsak.UTSETTELSE_GYLDIG_PGA_FERIE, false,false));

        Specification<FastsettePeriodeGrunnlag> sjekkOmFeriePåBevegeligHelligdag = rs.hvisRegel(SjekkOmFeriePåBevegeligHelligdag.ID, "Er det ferie på bevegelig helligdag?")
            .hvis(new SjekkOmFeriePåBevegeligHelligdag(), Manuellbehandling.opprett("UT1104", IkkeOppfyltÅrsak.UTSETTELSE_FERIE_PÅ_BEVEGELIG_HELLIGDAG, Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE, true, true))
            .ellers(sjekkOmBareFarHarRettNode);

        Specification<FastsettePeriodeGrunnlag> sjekkOmSøkerErArbeidstaker = rs.hvisRegel(SjekkOmSøkerErArbeidstaker.ID, "Er søker arbeidstaker?")
            .hvis(new SjekkOmSøkerErArbeidstaker(), sjekkOmFeriePåBevegeligHelligdag)
            .ellers(Manuellbehandling.opprett("UT1102", IkkeOppfyltÅrsak.FERIE_SELVSTENDIG_NÆRINGSDRIVENDSE_FRILANSER, Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE, true, false));

        Specification<FastsettePeriodeGrunnlag> sjekkOmUtsettelseEtterUke6 = rs.hvisRegel(SjekkOmUttakSkjerEtterDeFørsteUkene.ID, SjekkOmUttakSkjerEtterDeFørsteUkene.BESKRIVELSE)
            .hvis(new SjekkOmUttakSkjerEtterDeFørsteUkene(konfigurasjon), sjekkOmSøkerErArbeidstaker)
            .ellers(IkkeOppfylt.opprett("UT1101", IkkeOppfyltÅrsak.UTSETTELSE_INNENFOR_DE_FØRSTE_6_UKENE, true, true));

        Specification<FastsettePeriodeGrunnlag> sjekkOmSøknadGjelderFødsel = rs.hvisRegel(SjekkOmSøknadGjelderTerminEllerFødsel.ID, SjekkOmSøknadGjelderTerminEllerFødsel.BESKRIVELSE)
            .hvis(new SjekkOmSøknadGjelderTerminEllerFødsel(), sjekkOmUtsettelseEtterUke6)
            .ellers(sjekkOmSøkerErArbeidstaker);

        Specification<FastsettePeriodeGrunnlag> sjekkOmUtsettelseEtterSøknadMottattdato = rs.hvisRegel(SjekkOmErUtsettelseFørSøknadMottattdato.ID, SjekkOmErUtsettelseFørSøknadMottattdato.BESKRIVELSE)
                .hvis(new SjekkOmErUtsettelseFørSøknadMottattdato(), Manuellbehandling.opprett("UT1126", IkkeOppfyltÅrsak.SØKT_UTSETTELSE_FERIE_ETTER_PERIODEN_HAR_BEGYNT, Manuellbehandlingårsak.SØKNADSFRIST, true, false))
                .ellers(sjekkOmSøknadGjelderFødsel);

        return rs.hvisRegel(SjekkOmPeriodenStarterFørFamiliehendelse.ID, "Er utsettelsesperioden før termin/fødsel eller omsorgsovertakelse?")
                .hvis(new SjekkOmPeriodenStarterFørFamiliehendelse(), Manuellbehandling.opprett("UT1100", IkkeOppfyltÅrsak.UTSETTELSE_FØR_TERMIN_FØDSEL, Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE, true, false))
                .ellers(sjekkOmUtsettelseEtterSøknadMottattdato);
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUtsettelsePgaArbeid() {
        return rs.hvisRegel(SjekkOmUtsettelsePgaArbeid.ID, "Er det utsettelse pga arbeid?")
                .hvis(new SjekkOmUtsettelsePgaArbeid(), delregelForArbeid())
                .ellers(sjekkOmUtsettelsePgaSykdomSkade());
    }

    private Specification<FastsettePeriodeGrunnlag> delregelForArbeid() {
        Specification<FastsettePeriodeGrunnlag> sjekkOmBareFarHarRettNode = rs.hvisRegel(SjekkOmBareFarHarRett.ID, SjekkOmBareFarHarRett.BESKRIVELSE)
            .hvis(new SjekkOmBareFarHarRett(), Manuellbehandling.opprett("UT1112", null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT, true, false))
            .ellers(Oppfylt.opprett("UT1114", InnvilgetÅrsak.UTSETTELSE_GYLDIG_PGA_100_PROSENT_ARBEID, false, false));

        Specification<FastsettePeriodeGrunnlag> sjekkOmUtsettelseEtterUke6 = rs.hvisRegel(SjekkOmUttakSkjerEtterDeFørsteUkene.ID, SjekkOmUttakSkjerEtterDeFørsteUkene.BESKRIVELSE)
            .hvis(new SjekkOmUttakSkjerEtterDeFørsteUkene(konfigurasjon), sjekkOmBareFarHarRettNode)
            .ellers(Manuellbehandling.opprett("UT1111", IkkeOppfyltÅrsak.UTSETTELSE_INNENFOR_DE_FØRSTE_6_UKENE,
                    Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE, true, false));

        Specification<FastsettePeriodeGrunnlag> sjekkOmSøknadGjelderFødsel = rs.hvisRegel(SjekkOmSøknadGjelderTerminEllerFødsel.ID, SjekkOmSøknadGjelderTerminEllerFødsel.BESKRIVELSE)
            .hvis(new SjekkOmSøknadGjelderTerminEllerFødsel(), sjekkOmUtsettelseEtterUke6)
            .ellers(sjekkOmBareFarHarRettNode);

        Specification<FastsettePeriodeGrunnlag> sjekkOmSøkerErIArbeidPåHeltid = rs.hvisRegel(SjekkOmFulltArbeidForUtsettelse.ID,
            "Er søker i inntektsgivende arbeid på heltid i Norge i søknadsperioden for utsettelse?")
            .hvis(new SjekkOmFulltArbeidForUtsettelse(), sjekkOmSøknadGjelderFødsel)
            .ellers(Manuellbehandling.opprett("UT1110", IkkeOppfyltÅrsak.IKKE_HELTIDSARBEID, Manuellbehandlingårsak.IKKE_HELTIDSARBEID, true, false));

        Specification<FastsettePeriodeGrunnlag> sjekkOmUtsettelseEtterSøknadMottattdato = rs.hvisRegel(SjekkOmErUtsettelseFørSøknadMottattdato.ID, SjekkOmErUtsettelseFørSøknadMottattdato.BESKRIVELSE)
                .hvis(new SjekkOmErUtsettelseFørSøknadMottattdato(), Manuellbehandling.opprett("UT1127", IkkeOppfyltÅrsak.SØKT_UTSETTELSE_ARBEID_ETTER_PERIODEN_HAR_BEGYNT, Manuellbehandlingårsak.SØKNADSFRIST, true, false))
                .ellers(sjekkOmSøkerErIArbeidPåHeltid);

        return rs.hvisRegel(SjekkOmPeriodenStarterFørFamiliehendelse.ID, "Er utsettelsesperioden før termin/fødsel eller omsorgsovertakelse?")
                .hvis(new SjekkOmPeriodenStarterFørFamiliehendelse(), Manuellbehandling.opprett("UT1109", IkkeOppfyltÅrsak.UTSETTELSE_FØR_TERMIN_FØDSEL, Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE, true, false))
                .ellers(sjekkOmUtsettelseEtterSøknadMottattdato);
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUtsettelsePgaSykdomSkade() {
        return rs.hvisRegel(SjekkOmUtsettelsePgaSykdomSkade.ID, "Er det utsettelse pga søkers sykdom eller skade?")
                .hvis(new SjekkOmUtsettelsePgaSykdomSkade(), delregelForSøkerSykdomEllerSkade())
                .ellers(sjekkOmUtsettelsePgaSøkerInnleggelse());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUtsettelsePgaSøkerInnleggelse() {
        return rs.hvisRegel(SjekkOmUtsettelsePgaSøkerInnleggelse.ID, "Er det utsettelse pga søkers innleggelse i helseinstitusjon?")
                .hvis(new SjekkOmUtsettelsePgaSøkerInnleggelse(), delregelForSøkerInnlagt())
                .ellers(sjekkOmUtsettelsePgaHV());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUtsettelsePgaHV() {
        return rs.hvisRegel(SjekkOmUtsettelsePgaHV.ID, SjekkOmUtsettelsePgaHV.BESKRIVELSE)
                .hvis(new SjekkOmUtsettelsePgaHV(), delregelForHV())
                .ellers(sjekkOmUtsettelsePgaTiltakViaNav());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUtsettelsePgaTiltakViaNav() {
        return rs.hvisRegel(SjekkOmUtsettelsePgaTiltakViaNav.ID, SjekkOmUtsettelsePgaTiltakViaNav.BESKRIVELSE)
                .hvis(new SjekkOmUtsettelsePgaTiltakViaNav(), delregelForTiltakViaNav())
                .ellers(delregelForBarnInnlagt());
    }

    private Specification<FastsettePeriodeGrunnlag> delregelForSøkerSykdomEllerSkade() {
        Specification<FastsettePeriodeGrunnlag> sjekkOmBareFarHarRettNode = rs.hvisRegel(SjekkOmBareFarHarRett.ID, SjekkOmBareFarHarRett.BESKRIVELSE)
            .hvis(new SjekkOmBareFarHarRett(), Manuellbehandling.opprett("UT1121", null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT, true, false))
            .ellers(Oppfylt.opprett("UT1116", InnvilgetÅrsak.UTSETTELSE_GYLDIG_PGA_SYKDOM, false, false));

        return rs.hvisRegel(SjekkOmSykdomSkade.ID, "Er søker vurdert til ute av stand til å ta seg av barnet i perioden?")
            .hvis(new SjekkOmSykdomSkade(), sjekkOmBareFarHarRettNode)
            .ellers(Manuellbehandling.opprett("UT1115", IkkeOppfyltÅrsak.SØKERS_SYKDOM_SKADE_IKKE_OPPFYLT, Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> delregelForSøkerInnlagt() {
        Specification<FastsettePeriodeGrunnlag> sjekkOmBareFarHarRettNode = rs.hvisRegel(SjekkOmBareFarHarRett.ID, SjekkOmBareFarHarRett.BESKRIVELSE)
            .hvis(new SjekkOmBareFarHarRett(), Manuellbehandling.opprett("UT1122", null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT, true, false))
            .ellers(Oppfylt.opprett("UT1118", InnvilgetÅrsak.UTSETTELSE_GYLDIG_PGA_INNLEGGELSE, false, false));

        return rs.hvisRegel(SjekkOmSøkerInnlagt.ID, "Var søker innlagt på helseinstitusjon i perioden?")
            .hvis(new SjekkOmSøkerInnlagt(), sjekkOmBareFarHarRettNode)
            .ellers(Manuellbehandling.opprett("UT1117", IkkeOppfyltÅrsak.SØKERS_INNLEGGELSE_IKKE_OPPFYLT, Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> delregelForBarnInnlagt() {
        Specification<FastsettePeriodeGrunnlag> sjekkOmBareFarHarRettNode = rs.hvisRegel(SjekkOmBareFarHarRett.ID, SjekkOmBareFarHarRett.BESKRIVELSE)
                .hvis(new SjekkOmBareFarHarRett(), Manuellbehandling.opprett("UT1123", null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT, true, false))
                .ellers(Oppfylt.opprett("UT1120", InnvilgetÅrsak.UTSETTELSE_GYLDIG_PGA_BARN_INNLAGT, false, false));

        Specification<FastsettePeriodeGrunnlag> sjekkOmFørTermin = rs.hvisRegel(SjekkOmPeriodeErFørTermin.ID, SjekkOmPeriodeErFørTermin.BESKRIVELSE)
                .hvis(new SjekkOmPeriodeErFørTermin(), IkkeOppfylt.opprett("UT1124", IkkeOppfyltÅrsak.FRATREKK_PLEIEPENGER, true, false))
                .ellers(sjekkOmBareFarHarRettNode);

        Specification<FastsettePeriodeGrunnlag> sjekkOmUttakFørUke33 = rs.hvisRegel(SjekkOmFødselErFørUke33.ID, SjekkOmFødselErFørUke33.BESKRIVELSE)
                .hvis(new SjekkOmFødselErFørUke33(konfigurasjon), sjekkOmFørTermin)
                .ellers(sjekkOmBareFarHarRettNode);

        return rs.hvisRegel(SjekkOmBarnInnlagt.ID, "Var barnet innlagt på helseinstitusjon i perioden?")
                .hvis(new SjekkOmBarnInnlagt(), sjekkOmUttakFørUke33)
                .ellers(Manuellbehandling.opprett("UT1119", IkkeOppfyltÅrsak.BARNETS_INNLEGGELSE_IKKE_OPPFYLT, Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> delregelForHV() {
        var sjekkOmUtsettelseEtterUke6 = rs.hvisRegel(SjekkOmUttakSkjerEtterDeFørsteUkene.ID, SjekkOmUttakSkjerEtterDeFørsteUkene.BESKRIVELSE)
                .hvis(new SjekkOmUttakSkjerEtterDeFørsteUkene(konfigurasjon), Oppfylt.opprett("UT1131", InnvilgetÅrsak.UTSETTELSE_GYLDIG_PGA_100_PROSENT_ARBEID,
                        false, false))
                .ellers(Manuellbehandling.opprett("UT1130", IkkeOppfyltÅrsak.UTSETTELSE_INNENFOR_DE_FØRSTE_6_UKENE, Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE,
                        true, false));
        var sjekkOmUtsettelseFørMottattDato = rs.hvisRegel(SjekkOmErUtsettelseFørSøknadMottattdato.ID, SjekkOmErUtsettelseFørSøknadMottattdato.BESKRIVELSE)
                .hvis(new SjekkOmErUtsettelseFørSøknadMottattdato(), Manuellbehandling.opprett("UT1129", IkkeOppfyltÅrsak.SØKT_UTSETTELSE_ARBEID_ETTER_PERIODEN_HAR_BEGYNT,
                        Manuellbehandlingårsak.SØKNADSFRIST, true, false))
                .ellers(sjekkOmUtsettelseEtterUke6);
        return rs.hvisRegel(SjekkOmDokumentertHV.ID, SjekkOmDokumentertHV.BESKRIVELSE)
                .hvis(new SjekkOmDokumentertHV(), sjekkOmUtsettelseFørMottattDato)
                .ellers(Manuellbehandling.opprett("UT1128", IkkeOppfyltÅrsak.IKKE_HELTIDSARBEID, Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE,
                        true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> delregelForTiltakViaNav() {
        var sjekkOmUtsettelseEtterUke6 = rs.hvisRegel(SjekkOmUttakSkjerEtterDeFørsteUkene.ID, SjekkOmUttakSkjerEtterDeFørsteUkene.BESKRIVELSE)
                .hvis(new SjekkOmUttakSkjerEtterDeFørsteUkene(konfigurasjon), Oppfylt.opprett("UT1135", InnvilgetÅrsak.UTSETTELSE_GYLDIG_PGA_100_PROSENT_ARBEID,
                        false, false))
                .ellers(Manuellbehandling.opprett("UT1134", IkkeOppfyltÅrsak.UTSETTELSE_INNENFOR_DE_FØRSTE_6_UKENE, Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE,
                        true, false));
        var sjekkOmUtsettelseFørMottattDato = rs.hvisRegel(SjekkOmErUtsettelseFørSøknadMottattdato.ID, SjekkOmErUtsettelseFørSøknadMottattdato.BESKRIVELSE)
                .hvis(new SjekkOmErUtsettelseFørSøknadMottattdato(), Manuellbehandling.opprett("UT1133", IkkeOppfyltÅrsak.SØKT_UTSETTELSE_ARBEID_ETTER_PERIODEN_HAR_BEGYNT,
                        Manuellbehandlingårsak.SØKNADSFRIST, true, false))
                .ellers(sjekkOmUtsettelseEtterUke6);
        return rs.hvisRegel(SjekkOmDokumentertTiltakViaNav.ID, SjekkOmDokumentertTiltakViaNav.BESKRIVELSE)
                .hvis(new SjekkOmDokumentertTiltakViaNav(), sjekkOmUtsettelseFørMottattDato)
                .ellers(Manuellbehandling.opprett("UT1132", IkkeOppfyltÅrsak.IKKE_HELTIDSARBEID, Manuellbehandlingårsak.IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE,
                        true, false));
    }
}
