package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak.INNLEGGELSE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak.SYKDOM_ELLER_SKADE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.INNVILGET;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.MANUELL_BEHANDLING;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.MØDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype.FØDSEL;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Dokumentasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GyldigGrunnPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedInnleggelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedSykdomEllerSkade;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utbetalingsgrad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.felles.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Periode;

class OverføringOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    void innvilgelse_fedrekvote_overføring_når_bruker_en_tom_for_sine_konto_skal_gå_automatisk() {
        //Mødrekvote og fellesperiode brukes opp før øverføring av fedrekvote
        var fødselsdato = LocalDate.of(2020, 1, 21);
        var mødrekvote = oppgittPeriode(MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1));
        var fellesperiode = oppgittPeriode(FELLESPERIODE, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1));
        var overføring = OppgittPeriode.forOverføring(FEDREKVOTE, fødselsdato.plusWeeks(8), fødselsdato.plusWeeks(11),
            INNLEGGELSE, fødselsdato, fødselsdato);
        var kontoer = new Kontoer.Builder().konto(konto(FEDREKVOTE, 10))
                .konto(konto(MØDREKVOTE, 30))
                .konto(konto(FELLESPERIODE, 15))
                .konto(konto(FORELDREPENGER_FØR_FØDSEL, 15));
        var dok = new Dokumentasjon.Builder().periodeMedInnleggelse(
                new PeriodeMedInnleggelse(overføring.getFom(), overføring.getTom()))
                .gyldigGrunnPeriode(new GyldigGrunnPeriode(overføring.getFom(), overføring.getTom()));
        var grunnlag = basicGrunnlagMor(fødselsdato).kontoer(kontoer)
                .søknad(søknad(FØDSEL, mødrekvote, fellesperiode, overføring).dokumentasjon(dok));
        var resultat = fastsettPerioder(grunnlag);

        //         1. mk
        //         2. fellesperiode
        //         3. overføring innvilget
        //         4. overføring til manuell pga tom dager
        assertThat(resultat).hasSize(4);
        assertThat(resultat.get(2).getUttakPeriode().getOverføringÅrsak()).isEqualTo(INNLEGGELSE);
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(2).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                InnvilgetÅrsak.OVERFØRING_ANNEN_PART_INNLAGT);

        assertThat(resultat.get(3).getUttakPeriode().getOverføringÅrsak()).isEqualTo(INNLEGGELSE);
        assertThat(resultat.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(MANUELL_BEHANDLING);
        assertThat(resultat.get(3).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    void innvilgelse_mødrekvote_overføring_når_bruker_en_tom_for_sine_konto_skal_gå_automatisk() {
        //Fedrekvote og fellesperiode brukes opp før øverføring av mødrekvote
        var fødselsdato = LocalDate.of(2020, 1, 21);
        var fedrekvote = oppgittPeriode(FEDREKVOTE, fødselsdato.plusWeeks(9), fødselsdato.plusWeeks(11).minusDays(1));
        var overføring = OppgittPeriode.forOverføring(MØDREKVOTE, fødselsdato.plusWeeks(11), fødselsdato.plusWeeks(14),
            SYKDOM_ELLER_SKADE, fødselsdato, fødselsdato);
        var kontoer = new Kontoer.Builder().konto(konto(FEDREKVOTE, 10))
                .konto(konto(MØDREKVOTE, 40))
                .konto(konto(FELLESPERIODE, 15))
                .konto(konto(FORELDREPENGER_FØR_FØDSEL, 15));
        var dok = new Dokumentasjon.Builder().periodeMedSykdomEllerSkade(
                new PeriodeMedSykdomEllerSkade(overføring.getFom(), overføring.getTom()))
                .gyldigGrunnPeriode(new GyldigGrunnPeriode(overføring.getFom(), overføring.getTom()));
        var annenPart = new AnnenPart.Builder()
                .uttaksperiode(innvilget(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), MØDREKVOTE))
                .uttaksperiode(innvilget(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(9).minusDays(1), FELLESPERIODE));
        var grunnlag = basicGrunnlagFar(fødselsdato).kontoer(kontoer)
                .søknad(søknad(FØDSEL, fedrekvote, overføring).dokumentasjon(dok))
                .annenPart(annenPart);
        var resultat = fastsettPerioder(grunnlag);

        //         1. fk
        //         2. overføring innvilget
        //         3. overføring til manuell pga tom dager
        assertThat(resultat).hasSize(3);
        assertThat(resultat.get(1).getUttakPeriode().getOverføringÅrsak()).isEqualTo(SYKDOM_ELLER_SKADE);
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(INNVILGET);
        assertThat(resultat.get(1).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(
                InnvilgetÅrsak.OVERFØRING_ANNEN_PART_SYKDOM_SKADE);

        assertThat(resultat.get(2).getUttakPeriode().getOverføringÅrsak()).isEqualTo(SYKDOM_ELLER_SKADE);
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(MANUELL_BEHANDLING);
        assertThat(resultat.get(2).getUttakPeriode().getPeriodeResultatÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    private AnnenpartUttakPeriode innvilget(LocalDate fom, LocalDate tom, Stønadskontotype stønadskontotype) {
        var aktivitet = new AnnenpartUttakPeriodeAktivitet(AktivitetIdentifikator.forFrilans(), stønadskontotype,
                new Trekkdager(Virkedager.beregnAntallVirkedager(new Periode(fom, tom))), Utbetalingsgrad.FULL);
        return AnnenpartUttakPeriode.Builder.uttak(fom, tom).innvilget(true).uttakPeriodeAktivitet(aktivitet).build();
    }
}
