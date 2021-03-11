package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlagImpl;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GyldigGrunnPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.fpsak.nare.evaluation.Resultat;

public class SjekkOmGyldigUtsettelseMødrekvoteHelePeriodenTest {


    @Test
    public void ingenGyldigGrunnPeriode() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(2);

        var uttakPeriode = OppgittPeriode.forManglendeSøkt(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL).leggTilOppgittPeriode(uttakPeriode))
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

        var uttakPeriode = OppgittPeriode.forManglendeSøkt(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(uttakPeriode)
                        .medDokumentasjon(new Dokumentasjon.Builder().leggGyldigGrunnPeriode(
                                new GyldigGrunnPeriode(gyldigGrunnStart, gyldigGrunnSlutt))))
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

        var uttakPeriode = OppgittPeriode.forManglendeSøkt(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(uttakPeriode)
                        .medDokumentasjon(new Dokumentasjon.Builder().leggGyldigGrunnPeriode(
                                new GyldigGrunnPeriode(gyldigGrunnStart, gyldigGrunnSlutt))))
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

        var uttakPeriode = OppgittPeriode.forManglendeSøkt(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(uttakPeriode)
                        .medDokumentasjon(new Dokumentasjon.Builder().leggGyldigGrunnPeriode(
                                new GyldigGrunnPeriode(gyldigGrunnStart, gyldigGrunnSlutt))))
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

        var uttakPeriode = OppgittPeriode.forManglendeSøkt(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(uttakPeriode)
                        .medDokumentasjon(new Dokumentasjon.Builder().leggGyldigGrunnPeriode(
                                new GyldigGrunnPeriode(gyldigGrunnStart, gyldigGrunnSlutt))))
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

        var uttakPeriode = OppgittPeriode.forManglendeSøkt(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(uttakPeriode)
                        .medDokumentasjon(new Dokumentasjon.Builder().leggGyldigGrunnPeriode(
                                new GyldigGrunnPeriode(gyldigGrunnStart, gyldigGrunnSlutt))))
                .build();

        Resultat resultat = evaluer(uttakPeriode, grunnlag);
        assertThat(resultat).isEqualTo(Resultat.NEI);
    }

    @Test
    public void bareBegynnelsenOgSluttenAvPeriodenErGyldig() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);

        var uttakPeriode = OppgittPeriode.forManglendeSøkt(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(uttakPeriode)
                        .medDokumentasjon(new Dokumentasjon.Builder().leggGyldigGrunnPeriode(
                                new GyldigGrunnPeriode(periodeStart.minusDays(1), periodeStart.plusDays(7)))
                                .leggGyldigGrunnPeriode(new GyldigGrunnPeriode(periodeSlutt.minusDays(5), periodeSlutt.plusDays(1)))))
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

        var uttakPeriode = OppgittPeriode.forManglendeSøkt(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(uttakPeriode)
                        .medDokumentasjon(new Dokumentasjon.Builder().leggGyldigGrunnPeriode(
                                new GyldigGrunnPeriode(gyldigGrunnStart, gyldigGrunnSlutt))))
                .build();

        Resultat resultat = evaluer(uttakPeriode, grunnlag);
        assertThat(resultat).isEqualTo(Resultat.JA);
    }

    @Test
    public void helePeriodenGyldigMedFlereGyldigGrunnPerioder() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);

        var uttakPeriode = OppgittPeriode.forManglendeSøkt(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(uttakPeriode)
                        .medDokumentasjon(new Dokumentasjon.Builder().leggGyldigGrunnPeriode(
                                new GyldigGrunnPeriode(periodeStart, periodeStart.plusDays(7)))
                                .leggGyldigGrunnPeriode(new GyldigGrunnPeriode(periodeStart.plusDays(8), periodeSlutt))))
                .build();

        Resultat resultat = evaluer(uttakPeriode, grunnlag);
        assertThat(resultat).isEqualTo(Resultat.JA);
    }

    @Test
    public void helePeriodenGyldigMedFlereGyldigGrunnPerioderSomOverlapper() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);

        var uttakPeriode = OppgittPeriode.forManglendeSøkt(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder().medType(Søknadstype.FØDSEL)
                        .leggTilOppgittPeriode(uttakPeriode)
                        .medDokumentasjon(new Dokumentasjon.Builder().leggGyldigGrunnPeriode(
                                new GyldigGrunnPeriode(periodeStart, periodeStart.plusDays(10)))
                                .leggGyldigGrunnPeriode(new GyldigGrunnPeriode(periodeStart.plusDays(7), periodeSlutt))))
                .build();

        Resultat resultat = evaluer(uttakPeriode, grunnlag);
        assertThat(resultat).isEqualTo(Resultat.JA);
    }

    private Resultat evaluer(OppgittPeriode oppgittPeriode, RegelGrunnlag grunnlag) {
        return new SjekkOmGyldigUtsettelseMødrekvoteHelePerioden().evaluate(
                new FastsettePeriodeGrunnlagImpl(grunnlag, null, oppgittPeriode)).result();
    }
}
