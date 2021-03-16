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
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.fpsak.nare.evaluation.Resultat;

class SjekkOmFørsteDelAvPeriodenHarGyldigGrunnTest {

    @Test
    void førsteDelGyldigGrunn() {
        var periodeStart = LocalDate.now().plusMonths(1);
        var periodeSlutt = periodeStart.plusWeeks(6);

        var msp = OppgittPeriode.forManglendeSøkt(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder().leggTilOppgittPeriode(msp)
                        .medDokumentasjon(new Dokumentasjon.Builder().leggGyldigGrunnPeriode(
                                new GyldigGrunnPeriode(periodeStart, periodeStart.plusDays(1)))))
                .build();

        assertThat(evaluer(grunnlag, msp)).isEqualTo(Resultat.JA);
    }

    @Test
    void førsteDelIkkeGyldigGrunn() {
        var periodeStart = LocalDate.now().plusMonths(1);
        var periodeSlutt = periodeStart.plusWeeks(6);

        var msp = OppgittPeriode.forManglendeSøkt(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        var grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder().leggTilOppgittPeriode(msp)
                        .medDokumentasjon(new Dokumentasjon.Builder().leggGyldigGrunnPeriode(
                                new GyldigGrunnPeriode(periodeSlutt.minusDays(2), periodeSlutt))))
                .build();

        assertThat(evaluer(grunnlag, msp)).isEqualTo(Resultat.NEI);
    }

    private Resultat evaluer(RegelGrunnlag grunnlag, OppgittPeriode søknadsperiode) {
        var sjekkOmFørsteDelHarGyldigGrunn = new SjekkOmFørsteDelAvPeriodenHarGyldigGrunn();
        var evaluation = sjekkOmFørsteDelHarGyldigGrunn.evaluate(
                new FastsettePeriodeGrunnlagImpl(grunnlag, null, søknadsperiode));
        return evaluation.result();
    }

}
