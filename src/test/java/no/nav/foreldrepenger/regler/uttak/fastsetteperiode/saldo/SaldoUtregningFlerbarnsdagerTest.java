package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePerioderRegelOrkestrering.mapTilÅrsak;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall.InnvilgetÅrsak;
import org.junit.jupiter.api.Test;

class SaldoUtregningFlerbarnsdagerTest {

    @Test
    void
            skal_bare_trekke_flerbarnsdager_fra_perioder_med_flerbarnsdager_med_minsterett_hvis_perioden_trekker_minsterett() {

        var aktivitetIdentifikator = AktivitetIdentifikator.forFrilans();
        var periode1 = new FastsattUttakPeriode.Builder()
                .flerbarnsdager(true)
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .resultatÅrsak(mapTilÅrsak(InnvilgetÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT_UTEN_AKTIVITETSKRAV))
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(
                        new Trekkdager(5), Stønadskontotype.FORELDREPENGER, aktivitetIdentifikator)))
                .build();
        var periode2 = new FastsattUttakPeriode.Builder()
                .flerbarnsdager(true)
                .periodeResultatType(Perioderesultattype.AVSLÅTT)
                .resultatÅrsak(mapTilÅrsak(IkkeOppfyltÅrsak.AKTIVITET_UKJENT_UDOKUMENTERT))
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(
                        new Trekkdager(5), Stønadskontotype.FORELDREPENGER, aktivitetIdentifikator)))
                .build();
        var saldoUtregning = new SaldoUtregningFlerbarnsdager(
                List.of(periode1, periode2),
                List.of(),
                Set.of(aktivitetIdentifikator),
                new Trekkdager(10),
                new Trekkdager(10));

        assertThat(saldoUtregning.restSaldo()).isEqualTo(new Trekkdager(5));
    }

    @Test
    void skal_alltid_trekke_flerbarnsdager_fra_perioder_med_flerbarnsdager_hvis_uten_minsterett() {
        var aktivitetIdentifikator = AktivitetIdentifikator.forFrilans();
        var periode1 = new FastsattUttakPeriode.Builder()
                .flerbarnsdager(true)
                .periodeResultatType(Perioderesultattype.INNVILGET)
                .resultatÅrsak(mapTilÅrsak(InnvilgetÅrsak.FORELDREPENGER_KUN_FAR_HAR_RETT))
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(
                        new Trekkdager(5), Stønadskontotype.FORELDREPENGER, aktivitetIdentifikator)))
                .build();
        var periode2 = new FastsattUttakPeriode.Builder()
                .flerbarnsdager(true)
                .periodeResultatType(Perioderesultattype.AVSLÅTT)
                .resultatÅrsak(mapTilÅrsak(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN))
                .aktiviteter(List.of(new FastsattUttakPeriodeAktivitet(
                        new Trekkdager(5), Stønadskontotype.FORELDREPENGER, aktivitetIdentifikator)))
                .build();
        var saldoUtregning = new SaldoUtregningFlerbarnsdager(
                List.of(periode1, periode2),
                List.of(),
                Set.of(aktivitetIdentifikator),
                new Trekkdager(10),
                Trekkdager.ZERO);

        assertThat(saldoUtregning.restSaldo()).isEqualTo(Trekkdager.ZERO);
    }
}
