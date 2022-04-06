package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlagImpl;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GyldigGrunnPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningTjeneste;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;

class SjekkOmDelerAvPeriodenHarGyldigGrunnTest {

    @Test
    void begynnelsenAvPeriodenHarGyldigGrunn() {
        var periodeStart = LocalDate.now().plusMonths(1);
        var periodeSlutt = periodeStart.plusWeeks(2);
        var gyldigGrunnStart = periodeStart;
        var gyldigGrunnSlutt = periodeStart.plusDays(1);

        var søknadsperiode = manglendeSøktPeriode(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .søknad(new Søknad.Builder().oppgittPeriode(søknadsperiode)
                        .dokumentasjon(new Dokumentasjon.Builder().gyldigGrunnPeriode(
                                new GyldigGrunnPeriode(gyldigGrunnStart, gyldigGrunnSlutt))))
                .build();

        var evaluation = evaluer(søknadsperiode, grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.JA);
    }

    @Test
    void midtenAvPeriodenHarGyldigGrunn() {
        var periodeStart = LocalDate.now().plusMonths(1);
        var periodeSlutt = periodeStart.plusWeeks(6);
        var gyldigGrunnStart = periodeStart.plusWeeks(3);
        var gyldigGrunnSlutt = periodeStart.plusWeeks(4);

        var søknadsperiode = manglendeSøktPeriode(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .søknad(new Søknad.Builder().oppgittPeriode(søknadsperiode)
                        .dokumentasjon(new Dokumentasjon.Builder().gyldigGrunnPeriode(
                                new GyldigGrunnPeriode(gyldigGrunnStart, gyldigGrunnSlutt))))
                .build();

        var evaluation = evaluer(søknadsperiode, grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.JA);
    }

    @Test
    void sluttenAvPeriodenHarGyldigGrunn() {
        var periodeStart = LocalDate.now().plusMonths(1);
        var periodeSlutt = periodeStart.plusWeeks(6);
        var gyldigGrunnStart = periodeStart.plusWeeks(5);
        var gyldigGrunnSlutt = periodeSlutt;

        var søknadsperiode = manglendeSøktPeriode(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .søknad(new Søknad.Builder().oppgittPeriode(søknadsperiode)
                        .dokumentasjon(new Dokumentasjon.Builder().gyldigGrunnPeriode(
                                new GyldigGrunnPeriode(gyldigGrunnStart, gyldigGrunnSlutt))))
                .build();

        var evaluation = evaluer(søknadsperiode, grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.JA);
    }

    @Test
    void helePeriodenErUgyldig() {
        var periodeStart = LocalDate.now().plusMonths(1);
        var periodeSlutt = periodeStart.plusWeeks(6);

        var søknadsperiode = manglendeSøktPeriode(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .søknad(new Søknad.Builder().oppgittPeriode(søknadsperiode)
                        .dokumentasjon(new Dokumentasjon.Builder().gyldigGrunnPeriode(
                                new GyldigGrunnPeriode(periodeStart.minusWeeks(1), periodeStart.minusDays(1)))
                                .gyldigGrunnPeriode(new GyldigGrunnPeriode(periodeSlutt.plusDays(1), periodeSlutt.plusWeeks(1)))))
                .build();

        var evaluation = evaluer(søknadsperiode, grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
    }

    @Test
    void ingenGyldigGrunnPerioder() {
        var periodeStart = LocalDate.now().plusMonths(1);
        var periodeSlutt = periodeStart.plusWeeks(6);

        var søknadsperiode = manglendeSøktPeriode(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .søknad(new Søknad.Builder().oppgittPeriode(søknadsperiode))
                .build();

        var evaluation = evaluer(søknadsperiode, grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
    }

    private Evaluation evaluer(OppgittPeriode søknadsperiode, RegelGrunnlag grunnlag) {
        var saldoUtregningGrunnlag = SaldoUtregningGrunnlag.forUtregningAvDelerAvUttak(List.of(), List.of(), grunnlag.getKontoer(),
                søknadsperiode.getFom(), grunnlag.getArbeid().getAktiviteter(), grunnlag.getSøknad().getMottattTidspunkt(),
                grunnlag.getAnnenPart() == null ? null : grunnlag.getAnnenPart().getSisteSøknadMottattTidspunkt());
        return new SjekkOmDelerAvPeriodenHarGyldigGrunn().evaluate(
                new FastsettePeriodeGrunnlagImpl(grunnlag, SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag),
                        søknadsperiode));
    }

    private OppgittPeriode manglendeSøktPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom) {
        return OppgittPeriode.forManglendeSøkt(stønadskontotype, fom, tom);
    }

}
