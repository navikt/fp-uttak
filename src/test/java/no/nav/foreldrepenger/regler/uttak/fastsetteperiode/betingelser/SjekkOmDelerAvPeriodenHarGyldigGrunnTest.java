package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlagImpl;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GyldigGrunnPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningTjeneste;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;

public class SjekkOmDelerAvPeriodenHarGyldigGrunnTest {

    @Test
    public void begynnelsenAvPeriodenHarGyldigGrunn() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(2);
        LocalDate gyldigGrunnStart = periodeStart;
        LocalDate gyldigGrunnSlutt = periodeStart.plusDays(1);

        OppgittPeriode søknadsperiode = manglendeSøktPeriode(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder().leggTilOppgittPeriode(søknadsperiode)
                        .medDokumentasjon(new Dokumentasjon.Builder().leggGyldigGrunnPeriode(
                                new GyldigGrunnPeriode(gyldigGrunnStart, gyldigGrunnSlutt))))
                .build();

        Evaluation evaluation = evaluer(søknadsperiode, grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.JA);
    }

    @Test
    public void midtenAvPeriodenHarGyldigGrunn() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);
        LocalDate gyldigGrunnStart = periodeStart.plusWeeks(3);
        LocalDate gyldigGrunnSlutt = periodeStart.plusWeeks(4);

        OppgittPeriode søknadsperiode = manglendeSøktPeriode(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder().leggTilOppgittPeriode(søknadsperiode)
                        .medDokumentasjon(new Dokumentasjon.Builder().leggGyldigGrunnPeriode(
                                new GyldigGrunnPeriode(gyldigGrunnStart, gyldigGrunnSlutt))))
                .build();

        Evaluation evaluation = evaluer(søknadsperiode, grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.JA);
    }

    @Test
    public void sluttenAvPeriodenHarGyldigGrunn() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);
        LocalDate gyldigGrunnStart = periodeStart.plusWeeks(5);
        LocalDate gyldigGrunnSlutt = periodeSlutt;

        OppgittPeriode søknadsperiode = manglendeSøktPeriode(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder().leggTilOppgittPeriode(søknadsperiode)
                        .medDokumentasjon(new Dokumentasjon.Builder().leggGyldigGrunnPeriode(
                                new GyldigGrunnPeriode(gyldigGrunnStart, gyldigGrunnSlutt))))
                .build();

        Evaluation evaluation = evaluer(søknadsperiode, grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.JA);
    }

    @Test
    public void helePeriodenErUgyldig() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);

        OppgittPeriode søknadsperiode = manglendeSøktPeriode(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder().leggTilOppgittPeriode(søknadsperiode)
                        .medDokumentasjon(new Dokumentasjon.Builder().leggGyldigGrunnPeriode(
                                new GyldigGrunnPeriode(periodeStart.minusWeeks(1), periodeStart.minusDays(1)))
                                .leggGyldigGrunnPeriode(new GyldigGrunnPeriode(periodeSlutt.plusDays(1), periodeSlutt.plusWeeks(1)))))
                .build();

        Evaluation evaluation = evaluer(søknadsperiode, grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
    }

    @Test
    public void ingenGyldigGrunnPerioder() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);

        OppgittPeriode søknadsperiode = manglendeSøktPeriode(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder().leggTilOppgittPeriode(søknadsperiode))
                .build();

        Evaluation evaluation = evaluer(søknadsperiode, grunnlag);
        assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
    }

    private Evaluation evaluer(OppgittPeriode søknadsperiode, RegelGrunnlag grunnlag) {
        var saldoUtregningGrunnlag = SaldoUtregningGrunnlag.forUtregningAvDelerAvUttak(List.of(), List.of(), grunnlag.getKontoer(),
                søknadsperiode.getFom(), grunnlag.getArbeid().getAktiviteter());
        return new SjekkOmDelerAvPeriodenHarGyldigGrunn().evaluate(
                new FastsettePeriodeGrunnlagImpl(grunnlag, SaldoUtregningTjeneste.lagUtregning(saldoUtregningGrunnlag),
                        søknadsperiode));
    }

    private OppgittPeriode manglendeSøktPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom) {
        return OppgittPeriode.forManglendeSøkt(stønadskontotype, fom, tom);
    }

}
