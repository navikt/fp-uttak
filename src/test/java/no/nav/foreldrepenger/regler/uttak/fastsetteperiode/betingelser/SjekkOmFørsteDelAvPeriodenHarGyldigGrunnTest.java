package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlagImpl;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GyldigGrunnPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.RegelGrunnlagTestBuilder;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;

public class SjekkOmFørsteDelAvPeriodenHarGyldigGrunnTest {

    @Test
    public void førsteDelGyldigGrunn() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);

        var msp = OppgittPeriode.forManglendeSøkt(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder()
                        .leggTilOppgittPeriode(msp)
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggGyldigGrunnPeriode(new GyldigGrunnPeriode(periodeStart, periodeStart.plusDays(1)))))
                .build();

        assertThat(evaluer(grunnlag, msp)).isEqualTo(Resultat.JA);
    }

    @Test
    public void førsteDelIkkeGyldigGrunn() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);

        var msp = OppgittPeriode.forManglendeSøkt(Stønadskontotype.MØDREKVOTE, periodeStart, periodeSlutt);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder()
                        .leggTilOppgittPeriode(msp)
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggGyldigGrunnPeriode(new GyldigGrunnPeriode(periodeSlutt.minusDays(2), periodeSlutt))))
                .build();

        assertThat(evaluer(grunnlag, msp)).isEqualTo(Resultat.NEI);
    }

    private Resultat evaluer(RegelGrunnlag grunnlag, OppgittPeriode søknadsperiode) {
        SjekkOmFørsteDelAvPeriodenHarGyldigGrunn sjekkOmFørsteDelHarGyldigGrunn = new SjekkOmFørsteDelAvPeriodenHarGyldigGrunn();
        Evaluation evaluation = sjekkOmFørsteDelHarGyldigGrunn.evaluate(new FastsettePeriodeGrunnlagImpl(grunnlag, null, søknadsperiode));
        return evaluation.result();
    }

}
