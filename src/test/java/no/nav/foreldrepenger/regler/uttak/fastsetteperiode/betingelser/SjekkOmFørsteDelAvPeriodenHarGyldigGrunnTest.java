package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagImpl;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GyldigGrunnPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Oppholdårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.RegelGrunnlagTestBuilder;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;

public class SjekkOmFørsteDelAvPeriodenHarGyldigGrunnTest {

    @Test
    public void førsteDelGyldigGrunn() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);

        OppholdPeriode oppholdPeriode = new OppholdPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD,
                Oppholdårsaktype.MANGLENDE_SØKT_PERIODE, periodeStart, periodeSlutt, null, false);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(oppholdPeriode)
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggGyldigGrunnPerioder(new GyldigGrunnPeriode(periodeStart, periodeStart.plusDays(1)))
                                .build())
                        .build())
                .build();

        assertThat(evaluer(grunnlag, oppholdPeriode)).isEqualTo(Resultat.JA);
    }

    @Test
    public void førsteDelIkkeGyldigGrunn() {
        LocalDate periodeStart = LocalDate.now().plusMonths(1);
        LocalDate periodeSlutt = periodeStart.plusWeeks(6);

        OppholdPeriode søknadsperiode = new OppholdPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE, periodeStart, periodeSlutt, null, false);
        RegelGrunnlag grunnlag = RegelGrunnlagTestBuilder.create()
                .medSøknad(new Søknad.Builder()
                        .leggTilSøknadsperiode(søknadsperiode)
                        .medDokumentasjon(new Dokumentasjon.Builder()
                                .leggGyldigGrunnPerioder(new GyldigGrunnPeriode(periodeSlutt.minusDays(2), periodeSlutt))
                                .build())
                        .build())
                .build();

        assertThat(evaluer(grunnlag, søknadsperiode)).isEqualTo(Resultat.NEI);
    }

    private Resultat evaluer(RegelGrunnlag grunnlag, UttakPeriode søknadsperiode) {
        SjekkOmFørsteDelAvPeriodenHarGyldigGrunn sjekkOmFørsteDelHarGyldigGrunn = new SjekkOmFørsteDelAvPeriodenHarGyldigGrunn();
        Evaluation evaluation = sjekkOmFørsteDelHarGyldigGrunn.evaluate(new FastsettePeriodeGrunnlagImpl(grunnlag, null, søknadsperiode));
        return evaluation.result();
    }

}
