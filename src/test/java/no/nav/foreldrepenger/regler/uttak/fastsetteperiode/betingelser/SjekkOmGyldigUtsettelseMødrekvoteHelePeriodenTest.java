package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagImpl;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GyldigGrunnPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Oppholdårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Trekkdagertilstand;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder;
import no.nav.fpsak.nare.evaluation.Resultat;

public class SjekkOmGyldigUtsettelseMødrekvoteHelePeriodenTest {


    @Test
    public void ingenGyldigGrunnPeriode() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(2);

        OppholdPeriode uttakPeriode = new OppholdPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE,
                periodeStart, periodeSlutt, null, false);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(uttakPeriode))
                .build();

        Resultat resultat = evaluer(uttakPeriode, grunnlag);
        assertThat(resultat).isEqualTo(Resultat.NEI);
    }

    @Test
    public void helePeriodeErUgyldig() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(2);
        LocalDate gyldigGrunnStart = periodeStart.minusWeeks(1);
        LocalDate gyldigGrunnSlutt = periodeStart.minusDays(1);

        OppholdPeriode uttakPeriode = new OppholdPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE,
                periodeStart, periodeSlutt, null, false);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(uttakPeriode)
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggGyldigGrunnPerioder(new GyldigGrunnPeriode(gyldigGrunnStart, gyldigGrunnSlutt))))
                .build();

        Resultat resultat = evaluer(uttakPeriode, grunnlag);
        assertThat(resultat).isEqualTo(Resultat.NEI);
    }


    @Test
    public void bareBegynnelseAvPeriodeErGyldig() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);
        LocalDate gyldigGrunnStart = periodeStart.minusWeeks(1);
        LocalDate gyldigGrunnSlutt = periodeStart.plusWeeks(1);

        OppholdPeriode uttakPeriode = new OppholdPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE,
                periodeStart, periodeSlutt, null, false);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(uttakPeriode)
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggGyldigGrunnPerioder(new GyldigGrunnPeriode(gyldigGrunnStart, gyldigGrunnSlutt))))
                .build();

        Resultat resultat = evaluer(uttakPeriode, grunnlag);
        assertThat(resultat).isEqualTo(Resultat.NEI);
    }

    @Test
    public void sisteDagIPeriodenErUgyldig() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);
        LocalDate gyldigGrunnStart = periodeStart.minusWeeks(1);
        LocalDate gyldigGrunnSlutt = periodeSlutt.minusDays(1);

        OppholdPeriode uttakPeriode = new OppholdPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE,
                periodeStart, periodeSlutt, null, false);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(uttakPeriode)
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggGyldigGrunnPerioder(new GyldigGrunnPeriode(gyldigGrunnStart, gyldigGrunnSlutt))))
                .build();

        Resultat resultat = evaluer(uttakPeriode, grunnlag);
        assertThat(resultat).isEqualTo(Resultat.NEI);
    }

    @Test
    public void bareMidtenAvPeriodenErGyldig() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);
        LocalDate gyldigGrunnStart = periodeStart.plusWeeks(1);
        LocalDate gyldigGrunnSlutt = gyldigGrunnStart.plusDays(5);

        OppholdPeriode uttakPeriode = new OppholdPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE,
                periodeStart, periodeSlutt, null, false);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(uttakPeriode)
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggGyldigGrunnPerioder(new GyldigGrunnPeriode(gyldigGrunnStart, gyldigGrunnSlutt))))
                .build();

        Resultat resultat = evaluer(uttakPeriode, grunnlag);
        assertThat(resultat).isEqualTo(Resultat.NEI);
    }

    @Test
    public void bareSluttenAvPeriodenErGyldig() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);
        LocalDate gyldigGrunnStart = periodeSlutt.minusWeeks(1);
        LocalDate gyldigGrunnSlutt = periodeSlutt.plusDays(5);

        OppholdPeriode uttakPeriode = new OppholdPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE,
                periodeStart, periodeSlutt, null, false);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(uttakPeriode)
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggGyldigGrunnPerioder(new GyldigGrunnPeriode(gyldigGrunnStart, gyldigGrunnSlutt))))
                .build();

        Resultat resultat = evaluer(uttakPeriode, grunnlag);
        assertThat(resultat).isEqualTo(Resultat.NEI);
    }

    @Test
    public void bareBegynnelsenOgSluttenAvPeriodenErGyldig() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);

        OppholdPeriode uttakPeriode = new OppholdPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE,
                periodeStart, periodeSlutt, null, false);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(uttakPeriode)
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggGyldigGrunnPerioder(new GyldigGrunnPeriode(periodeStart.minusDays(1), periodeStart.plusDays(7)))
                                .leggGyldigGrunnPerioder(new GyldigGrunnPeriode(periodeSlutt.minusDays(5), periodeSlutt.plusDays(1)))))
                .build();

        Resultat resultat = evaluer(uttakPeriode, grunnlag);
        assertThat(resultat).isEqualTo(Resultat.NEI);
    }

    @Test
    public void helePeriodenGyldig() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);
        LocalDate gyldigGrunnStart = periodeStart;
        LocalDate gyldigGrunnSlutt = periodeSlutt;

        OppholdPeriode uttakPeriode = new OppholdPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE,
                periodeStart, periodeSlutt, null, false);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(uttakPeriode)
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggGyldigGrunnPerioder(new GyldigGrunnPeriode(gyldigGrunnStart, gyldigGrunnSlutt))))
                .build();

        Resultat resultat = evaluer(uttakPeriode, grunnlag);
        assertThat(resultat).isEqualTo(Resultat.JA);
    }

    @Test
    public void helePeriodenGyldigMedFlereGyldigGrunnPerioder() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);

        OppholdPeriode uttakPeriode = new OppholdPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE,
                periodeStart, periodeSlutt, null, false);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(uttakPeriode)
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggGyldigGrunnPerioder(new GyldigGrunnPeriode(periodeStart, periodeStart.plusDays(7)))
                                .leggGyldigGrunnPerioder(new GyldigGrunnPeriode(periodeStart.plusDays(8), periodeSlutt))))
                .build();

        Resultat resultat = evaluer(uttakPeriode, grunnlag);
        assertThat(resultat).isEqualTo(Resultat.JA);
    }

    @Test
    public void helePeriodenGyldigMedFlereGyldigGrunnPerioderSomOverlapper() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);

        OppholdPeriode uttakPeriode = new OppholdPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE,
                periodeStart, periodeSlutt, null, false);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder()
                        .medType(Søknadstype.FØDSEL)
                        .leggTilSøknadsperiode(uttakPeriode)
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggGyldigGrunnPerioder(new GyldigGrunnPeriode(periodeStart, periodeStart.plusDays(10)))
                                .leggGyldigGrunnPerioder(new GyldigGrunnPeriode(periodeStart.plusDays(7), periodeSlutt))))
                .build();

        Resultat resultat = evaluer(uttakPeriode, grunnlag);
        assertThat(resultat).isEqualTo(Resultat.JA);
    }

    private Resultat evaluer(OppholdPeriode uttakPeriode, RegelGrunnlag grunnlag) {
        return new SjekkOmGyldigUtsettelseMødrekvoteHelePerioden().evaluate(new FastsettePeriodeGrunnlagImpl(grunnlag,
                Trekkdagertilstand.ny(grunnlag, Collections.singletonList(uttakPeriode)), uttakPeriode)).result();
    }
}
