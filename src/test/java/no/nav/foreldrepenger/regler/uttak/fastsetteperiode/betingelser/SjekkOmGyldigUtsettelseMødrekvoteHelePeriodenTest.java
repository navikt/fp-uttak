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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.fpsak.nare.evaluation.Resultat;

class SjekkOmGyldigUtsettelseMødrekvoteHelePeriodenTest {


    @Test
    void ingenGyldigGrunnPeriode() {
        var periodeStart = LocalDate.now().plusMonths(1);
        var periodeSlutt = periodeStart.plusWeeks(2);

        var uttakPeriode = OppgittPeriode.forManglendeSøkt(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL).oppgittPeriode(uttakPeriode))
                .build();

        var resultat = evaluer(uttakPeriode, grunnlag);
        assertThat(resultat).isEqualTo(Resultat.NEI);
    }

    @Test
    void helePeriodeErUgyldig() {
        var periodeStart = LocalDate.now().plusMonths(1);
        var periodeSlutt = periodeStart.plusWeeks(2);
        var gyldigGrunnStart = periodeStart.minusWeeks(1);
        var gyldigGrunnSlutt = periodeStart.minusDays(1);

        var uttakPeriode = OppgittPeriode.forManglendeSøkt(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(uttakPeriode)
                        .dokumentasjon(new Dokumentasjon.Builder().gyldigGrunnPeriode(
                                new GyldigGrunnPeriode(gyldigGrunnStart, gyldigGrunnSlutt))))
                .build();

        var resultat = evaluer(uttakPeriode, grunnlag);
        assertThat(resultat).isEqualTo(Resultat.NEI);
    }


    @Test
    void bareBegynnelseAvPeriodeErGyldig() {
        var periodeStart = LocalDate.now().plusMonths(1);
        var periodeSlutt = periodeStart.plusWeeks(6);
        var gyldigGrunnStart = periodeStart.minusWeeks(1);
        var gyldigGrunnSlutt = periodeStart.plusWeeks(1);

        var uttakPeriode = OppgittPeriode.forManglendeSøkt(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(uttakPeriode)
                        .dokumentasjon(new Dokumentasjon.Builder().gyldigGrunnPeriode(
                                new GyldigGrunnPeriode(gyldigGrunnStart, gyldigGrunnSlutt))))
                .build();

        var resultat = evaluer(uttakPeriode, grunnlag);
        assertThat(resultat).isEqualTo(Resultat.NEI);
    }

    @Test
    void sisteDagIPeriodenErUgyldig() {
        var periodeStart = LocalDate.now().plusMonths(1);
        var periodeSlutt = periodeStart.plusWeeks(6);
        var gyldigGrunnStart = periodeStart.minusWeeks(1);
        var gyldigGrunnSlutt = periodeSlutt.minusDays(1);

        var uttakPeriode = OppgittPeriode.forManglendeSøkt(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(uttakPeriode)
                        .dokumentasjon(new Dokumentasjon.Builder().gyldigGrunnPeriode(
                                new GyldigGrunnPeriode(gyldigGrunnStart, gyldigGrunnSlutt))))
                .build();

        var resultat = evaluer(uttakPeriode, grunnlag);
        assertThat(resultat).isEqualTo(Resultat.NEI);
    }

    @Test
    void bareMidtenAvPeriodenErGyldig() {
        var periodeStart = LocalDate.now().plusMonths(1);
        var periodeSlutt = periodeStart.plusWeeks(6);
        var gyldigGrunnStart = periodeStart.plusWeeks(1);
        var gyldigGrunnSlutt = gyldigGrunnStart.plusDays(5);

        var uttakPeriode = OppgittPeriode.forManglendeSøkt(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(uttakPeriode)
                        .dokumentasjon(new Dokumentasjon.Builder().gyldigGrunnPeriode(
                                new GyldigGrunnPeriode(gyldigGrunnStart, gyldigGrunnSlutt))))
                .build();

        var resultat = evaluer(uttakPeriode, grunnlag);
        assertThat(resultat).isEqualTo(Resultat.NEI);
    }

    @Test
    void bareSluttenAvPeriodenErGyldig() {
        var periodeStart = LocalDate.now().plusMonths(1);
        var periodeSlutt = periodeStart.plusWeeks(6);
        var gyldigGrunnStart = periodeSlutt.minusWeeks(1);
        var gyldigGrunnSlutt = periodeSlutt.plusDays(5);

        var uttakPeriode = OppgittPeriode.forManglendeSøkt(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(uttakPeriode)
                        .dokumentasjon(new Dokumentasjon.Builder().gyldigGrunnPeriode(
                                new GyldigGrunnPeriode(gyldigGrunnStart, gyldigGrunnSlutt))))
                .build();

        var resultat = evaluer(uttakPeriode, grunnlag);
        assertThat(resultat).isEqualTo(Resultat.NEI);
    }

    @Test
    void bareBegynnelsenOgSluttenAvPeriodenErGyldig() {
        var periodeStart = LocalDate.now().plusMonths(1);
        var periodeSlutt = periodeStart.plusWeeks(6);

        var uttakPeriode = OppgittPeriode.forManglendeSøkt(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(uttakPeriode)
                        .dokumentasjon(new Dokumentasjon.Builder().gyldigGrunnPeriode(
                                new GyldigGrunnPeriode(periodeStart.minusDays(1), periodeStart.plusDays(7)))
                                .gyldigGrunnPeriode(new GyldigGrunnPeriode(periodeSlutt.minusDays(5), periodeSlutt.plusDays(1)))))
                .build();

        var resultat = evaluer(uttakPeriode, grunnlag);
        assertThat(resultat).isEqualTo(Resultat.NEI);
    }

    @Test
    void helePeriodenGyldig() {
        var periodeStart = LocalDate.now().plusMonths(1);
        var periodeSlutt = periodeStart.plusWeeks(6);
        var gyldigGrunnStart = periodeStart;
        var gyldigGrunnSlutt = periodeSlutt;

        var uttakPeriode = OppgittPeriode.forManglendeSøkt(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(uttakPeriode)
                        .dokumentasjon(new Dokumentasjon.Builder().gyldigGrunnPeriode(
                                new GyldigGrunnPeriode(gyldigGrunnStart, gyldigGrunnSlutt))))
                .build();

        var resultat = evaluer(uttakPeriode, grunnlag);
        assertThat(resultat).isEqualTo(Resultat.JA);
    }

    @Test
    void helePeriodenGyldigMedFlereGyldigGrunnPerioder() {
        var periodeStart = LocalDate.now().plusMonths(1);
        var periodeSlutt = periodeStart.plusWeeks(6);

        var uttakPeriode = OppgittPeriode.forManglendeSøkt(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(uttakPeriode)
                        .dokumentasjon(new Dokumentasjon.Builder().gyldigGrunnPeriode(
                                new GyldigGrunnPeriode(periodeStart, periodeStart.plusDays(7)))
                                .gyldigGrunnPeriode(new GyldigGrunnPeriode(periodeStart.plusDays(8), periodeSlutt))))
                .build();

        var resultat = evaluer(uttakPeriode, grunnlag);
        assertThat(resultat).isEqualTo(Resultat.JA);
    }

    @Test
    void helePeriodenGyldigMedFlereGyldigGrunnPerioderSomOverlapper() {
        var periodeStart = LocalDate.now().plusMonths(1);
        var periodeSlutt = periodeStart.plusWeeks(6);

        var uttakPeriode = OppgittPeriode.forManglendeSøkt(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .søknad(new Søknad.Builder().type(Søknadstype.FØDSEL)
                        .oppgittPeriode(uttakPeriode)
                        .dokumentasjon(new Dokumentasjon.Builder().gyldigGrunnPeriode(
                                new GyldigGrunnPeriode(periodeStart, periodeStart.plusDays(10)))
                                .gyldigGrunnPeriode(new GyldigGrunnPeriode(periodeStart.plusDays(7), periodeSlutt))))
                .build();

        var resultat = evaluer(uttakPeriode, grunnlag);
        assertThat(resultat).isEqualTo(Resultat.JA);
    }

    private Resultat evaluer(OppgittPeriode oppgittPeriode, RegelGrunnlag grunnlag) {
        return new SjekkOmGyldigUtsettelseMødrekvoteHelePerioden().evaluate(
                new FastsettePeriodeGrunnlagImpl(grunnlag, null, null, oppgittPeriode)).result();
    }
}
