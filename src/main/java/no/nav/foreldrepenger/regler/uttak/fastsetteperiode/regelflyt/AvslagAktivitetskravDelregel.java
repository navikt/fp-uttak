package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.regelflyt;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_ARBEID_IKKE_DOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_ARBEID_IKKE_OPPFYLT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_DELTAKELSE_INTRODUKSJONSPROGRAM_IKKE_DOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_DELTAKELSE_INTRODUKSJONSPROGRAM_IKKE_OPPFYLT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_DELTAKELSE_KVALIFISERINGSPROGRAM_IKKE_DOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_DELTAKELSE_KVALIFISERINGSPROGRAM_IKKE_OPPFYLT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_INNLEGGELSE_IKKE_DOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_INNLEGGELSE_IKKE_OPPFYLT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_KOMBINASJON_ARBEID_UTDANNING_IKKE_DOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_KOMBINASJON_ARBEID_UTDANNING_IKKE_OPPFYLT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_SYKDOM_IKKE_DOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_SYKDOM_IKKE_OPPFYLT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_UTDANNING_IKKE_DOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITETSKRAVET_UTDANNING_IKKE_OPPFYLT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.AKTIVITET_UKJENT_UDOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.BARE_FAR_RETT_IKKE_SØKT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT_MOR_IKKE_UFØR;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.SjekkOmTomForAlleSineKontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav.SjekkOmAktivitetErDokumentert;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav.SjekkOmFriUtsettelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav.SjekkOmMorArbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav.SjekkOmMorBekreftetUføre;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav.SjekkOmMorInnlagt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav.SjekkOmMorIntroduksjonsprogram;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav.SjekkOmMorKombinasjonArbeidUtdanning;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav.SjekkOmMorKvalifiseringsprogram;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav.SjekkOmMorOppgittUføre;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav.SjekkOmMorSyk;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav.SjekkOmMorUtdanning;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav.SjekkOmMorsAktivitetErKjent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav.SjekkOmSkalTrekkeDagerFraKonto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav.SjekkOmUttakOgUtenAktivitetskrav;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.FastsettePeriodeUtfall;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfylt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.Manuellbehandlingårsak;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.specification.Specification;

@RuleDocumentation(value = AvslagAktivitetskravDelregel.ID, specificationReference = "https://confluence.adeo.no/display/MODNAV/1.+Samleside+for+oppdaterte+regelflyter")
public class AvslagAktivitetskravDelregel implements RuleService<FastsettePeriodeGrunnlag> {

    public static final String ID = "AVSLAG_AKT";

    private final Ruleset<FastsettePeriodeGrunnlag> rs = new Ruleset<>();


    @Override
    public Specification<FastsettePeriodeGrunnlag> getSpecification() {
        return rs.hvisRegel(SjekkOmTomForAlleSineKontoer.ID, SjekkOmTomForAlleSineKontoer.BESKRIVELSE)
            .hvis(new SjekkOmTomForAlleSineKontoer(), IkkeOppfylt.opprett("UT1319", IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, false, false))
            .ellers(sjekkOmKjentAktivitet());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmKjentAktivitet() {
        return rs.hvisRegel(SjekkOmMorsAktivitetErKjent.ID, SjekkOmMorsAktivitetErKjent.BESKRIVELSE)
            .hvis(new SjekkOmMorsAktivitetErKjent(), sjekkOmMorArbeid())
            .ellers(sjekkOmUkjentAktivitetErUttakUtenAktivitetskrav());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmMorArbeid() {
        return rs.hvisRegel(SjekkOmMorArbeid.ID, SjekkOmMorArbeid.BESKRIVELSE).hvis(new SjekkOmMorArbeid(), morArbeid()).ellers(sjekkOmMorSyk());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmMorSyk() {
        return rs.hvisRegel(SjekkOmMorSyk.ID, SjekkOmMorSyk.BESKRIVELSE).hvis(new SjekkOmMorSyk(), morSyk()).ellers(sjekkOmMorInnlagt());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmMorInnlagt() {
        return rs.hvisRegel(SjekkOmMorInnlagt.ID, SjekkOmMorInnlagt.BESKRIVELSE)
            .hvis(new SjekkOmMorInnlagt(), morInnlagt())
            .ellers(sjekkOmMorUtdanning());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmMorUtdanning() {
        return rs.hvisRegel(SjekkOmMorUtdanning.ID, SjekkOmMorUtdanning.BESKRIVELSE)
            .hvis(new SjekkOmMorUtdanning(), morUtdannning())
            .ellers(sjekkOmMorKvalifiseringsprogram());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmMorKvalifiseringsprogram() {
        return rs.hvisRegel(SjekkOmMorKvalifiseringsprogram.ID, SjekkOmMorKvalifiseringsprogram.BESKRIVELSE)
            .hvis(new SjekkOmMorKvalifiseringsprogram(), morKvalifiseringsprogram())
            .ellers(sjekkOmMorIntroduksjonsprogram());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmMorIntroduksjonsprogram() {
        return rs.hvisRegel(SjekkOmMorIntroduksjonsprogram.ID, SjekkOmMorIntroduksjonsprogram.BESKRIVELSE)
            .hvis(new SjekkOmMorIntroduksjonsprogram(), morIntroduksjonsprogram())
            .ellers(sjekkOmMorKombinasjonArbeidUtdanning());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmMorKombinasjonArbeidUtdanning() {
        return rs.hvisRegel(SjekkOmMorKombinasjonArbeidUtdanning.ID, SjekkOmMorKombinasjonArbeidUtdanning.BESKRIVELSE)
            .hvis(new SjekkOmMorKombinasjonArbeidUtdanning(), morKombinasjonArbeidUtdanning())
            .ellers(sjekkOmMorOppgittUføretrygd());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmMorOppgittUføretrygd() {
        return rs.hvisRegel(SjekkOmMorOppgittUføre.ID, SjekkOmMorOppgittUføre.BESKRIVELSE)
            .hvis(new SjekkOmMorOppgittUføre(), sjekkOmMorBekreftetUføretrygd())
            .ellers(utfall1314AvklarAktivitetSituasjon());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmMorBekreftetUføretrygd() {
        return rs.hvisRegel(SjekkOmMorBekreftetUføre.ID, SjekkOmMorBekreftetUføre.BESKRIVELSE)
            .hvis(new SjekkOmMorBekreftetUføre(), sjekkOmMorBekreftetUføretrygdErUttak())
            .ellers(avslåSjekkSkalTrekkeDager("UT1322", FORELDREPENGER_KUN_FAR_HAR_RETT_MOR_IKKE_UFØR));
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmMorBekreftetUføretrygdErUttak() {
        return rs.hvisRegel(SjekkOmUttakOgUtenAktivitetskrav.ID, SjekkOmUttakOgUtenAktivitetskrav.BESKRIVELSE)
            .hvis(new SjekkOmUttakOgUtenAktivitetskrav(), avslå("UT1324", AKTIVITET_UKJENT_UDOKUMENTERT))
            .ellers(utfall1314AvklarAktivitetSituasjon());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUkjentAktivitetErUttakUtenAktivitetskrav() {
        return rs.hvisRegel(SjekkOmUttakOgUtenAktivitetskrav.ID, SjekkOmUttakOgUtenAktivitetskrav.BESKRIVELSE)
            .hvis(new SjekkOmUttakOgUtenAktivitetskrav(), avslå("UT1323", AKTIVITET_UKJENT_UDOKUMENTERT))
            .ellers(sjekkOmUkjentAktivitetErFriUtsettelse());
    }

    private Specification<FastsettePeriodeGrunnlag> sjekkOmUkjentAktivitetErFriUtsettelse() {
        // Pga praksis med tekniske perioder u/aktivitet fra søknader fram til første uttak.
        return rs.hvisRegel(SjekkOmFriUtsettelse.ID, SjekkOmFriUtsettelse.BESKRIVELSE)
            .hvis(new SjekkOmFriUtsettelse(), avslåSjekkSkalTrekkeDager("UT1325", BARE_FAR_RETT_IKKE_SØKT))
            .ellers(Manuellbehandling.opprett("UT1315", null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT, true, false));
    }

    private Specification<FastsettePeriodeGrunnlag> morArbeid() {
        return rs.hvisRegel(SjekkOmAktivitetErDokumentert.ID, SjekkOmAktivitetErDokumentert.BESKRIVELSE)
            .hvis(new SjekkOmAktivitetErDokumentert(), avslåSjekkSkalTrekkeDager("UT1300", AKTIVITETSKRAVET_ARBEID_IKKE_OPPFYLT))
            .ellers(avslåSjekkSkalTrekkeDager("UT1301", AKTIVITETSKRAVET_ARBEID_IKKE_DOKUMENTERT));
    }

    private Specification<FastsettePeriodeGrunnlag> morSyk() {
        return rs.hvisRegel(SjekkOmAktivitetErDokumentert.ID, SjekkOmAktivitetErDokumentert.BESKRIVELSE)
            .hvis(new SjekkOmAktivitetErDokumentert(), avslåSjekkSkalTrekkeDager("UT1302", AKTIVITETSKRAVET_SYKDOM_IKKE_OPPFYLT))
            .ellers(avslåSjekkSkalTrekkeDager("UT1303", AKTIVITETSKRAVET_SYKDOM_IKKE_DOKUMENTERT));
    }

    private Specification<FastsettePeriodeGrunnlag> morInnlagt() {
        return rs.hvisRegel(SjekkOmAktivitetErDokumentert.ID, SjekkOmAktivitetErDokumentert.BESKRIVELSE)
            .hvis(new SjekkOmAktivitetErDokumentert(), avslåSjekkSkalTrekkeDager("UT1304", AKTIVITETSKRAVET_INNLEGGELSE_IKKE_OPPFYLT))
            .ellers(avslåSjekkSkalTrekkeDager("UT1305", AKTIVITETSKRAVET_INNLEGGELSE_IKKE_DOKUMENTERT));
    }

    private Specification<FastsettePeriodeGrunnlag> morUtdannning() {
        return rs.hvisRegel(SjekkOmAktivitetErDokumentert.ID, SjekkOmAktivitetErDokumentert.BESKRIVELSE)
            .hvis(new SjekkOmAktivitetErDokumentert(), avslåSjekkSkalTrekkeDager("UT1306", AKTIVITETSKRAVET_UTDANNING_IKKE_OPPFYLT))
            .ellers(avslåSjekkSkalTrekkeDager("UT1307", AKTIVITETSKRAVET_UTDANNING_IKKE_DOKUMENTERT));
    }

    private Specification<FastsettePeriodeGrunnlag> morKvalifiseringsprogram() {
        return rs.hvisRegel(SjekkOmAktivitetErDokumentert.ID, SjekkOmAktivitetErDokumentert.BESKRIVELSE)
            .hvis(new SjekkOmAktivitetErDokumentert(),
                avslåSjekkSkalTrekkeDager("UT1308", AKTIVITETSKRAVET_DELTAKELSE_KVALIFISERINGSPROGRAM_IKKE_OPPFYLT))
            .ellers(avslåSjekkSkalTrekkeDager("UT1309", AKTIVITETSKRAVET_DELTAKELSE_KVALIFISERINGSPROGRAM_IKKE_DOKUMENTERT));
    }

    private Specification<FastsettePeriodeGrunnlag> morIntroduksjonsprogram() {
        return rs.hvisRegel(SjekkOmAktivitetErDokumentert.ID, SjekkOmAktivitetErDokumentert.BESKRIVELSE)
            .hvis(new SjekkOmAktivitetErDokumentert(),
                avslåSjekkSkalTrekkeDager("UT1310", AKTIVITETSKRAVET_DELTAKELSE_INTRODUKSJONSPROGRAM_IKKE_OPPFYLT))
            .ellers(avslåSjekkSkalTrekkeDager("UT1311", AKTIVITETSKRAVET_DELTAKELSE_INTRODUKSJONSPROGRAM_IKKE_DOKUMENTERT));
    }

    private Specification<FastsettePeriodeGrunnlag> morKombinasjonArbeidUtdanning() {
        return rs.hvisRegel(SjekkOmAktivitetErDokumentert.ID, SjekkOmAktivitetErDokumentert.BESKRIVELSE)
            .hvis(new SjekkOmAktivitetErDokumentert(),
                avslåSjekkSkalTrekkeDager("UT1312", AKTIVITETSKRAVET_KOMBINASJON_ARBEID_UTDANNING_IKKE_OPPFYLT))
            .ellers(avslåSjekkSkalTrekkeDager("UT1313", AKTIVITETSKRAVET_KOMBINASJON_ARBEID_UTDANNING_IKKE_DOKUMENTERT));
    }

    private Specification<FastsettePeriodeGrunnlag> avslåSjekkSkalTrekkeDager(String sluttpunktId, IkkeOppfyltÅrsak årsak) {
        return rs.hvisRegel(SjekkOmAktivitetErDokumentert.ID, SjekkOmAktivitetErDokumentert.BESKRIVELSE)
            .hvis(new SjekkOmSkalTrekkeDagerFraKonto(), IkkeOppfylt.opprett(sluttpunktId, årsak, true, false))
            .ellers(IkkeOppfylt.opprett(sluttpunktId, årsak, false, false));
    }

    private FastsettePeriodeUtfall avslå(String sluttpunktId, IkkeOppfyltÅrsak årsak) {
        return IkkeOppfylt.opprett(sluttpunktId, årsak, true, false);
    }

    private Specification<FastsettePeriodeGrunnlag> utfall1314AvklarAktivitetSituasjon() {
        return rs.hvisRegel(SjekkOmAktivitetErDokumentert.ID, SjekkOmAktivitetErDokumentert.BESKRIVELSE)
            .hvis(new SjekkOmSkalTrekkeDagerFraKonto(), Manuellbehandling.opprett("UT1314", null, Manuellbehandlingårsak.MOR_UFØR, true, true))
            .ellers(Manuellbehandling.opprett("UT1314", null, Manuellbehandlingårsak.MOR_UFØR, false, true));
    }
}
