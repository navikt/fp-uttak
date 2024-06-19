package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningUtil.aktivitetIPeriode;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningUtil.aktiviteterIPerioder;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningUtil.innvilgetMedTrekkdager;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningUtil.overlappendePeriode;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningUtil.trekkDagerFraDelAvPeriode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattUttakPeriodeAktivitet;

class SaldoUtregningFlerbarnsdager {

    private final List<FastsattUttakPeriode> søkersPerioder;
    private final List<FastsattUttakPeriode> annenpartsPerioder;
    private final Set<AktivitetIdentifikator> søkersAktiviteter;
    private final Trekkdager flerbarnsdager;
    private final Trekkdager minsterettDager;

    public SaldoUtregningFlerbarnsdager(
            List<FastsattUttakPeriode> søkersPerioder,
            List<FastsattUttakPeriode> annenpartsPerioder,
            Set<AktivitetIdentifikator> søkersAktiviteter,
            Trekkdager flerbarnsdager,
            Trekkdager minsterettDager) {

        this.søkersPerioder = søkersPerioder;
        this.annenpartsPerioder = annenpartsPerioder;
        this.søkersAktiviteter = søkersAktiviteter;
        this.flerbarnsdager = flerbarnsdager;
        this.minsterettDager = minsterettDager;
    }

    Trekkdager restSaldo() {
        return søkersAktiviteter.stream()
                .map(this::restSaldo)
                .max(Trekkdager::compareTo)
                .orElse(Trekkdager.ZERO);
    }

    Trekkdager restSaldo(AktivitetIdentifikator aktivitet) {
        var forbruktSøker = forbruktSøker(aktivitet, søkersPerioder);
        var forbruktAnnenpart = minForbruktAnnenpart();
        // frigitte dager er dager fra annenpart som blir ledig når søker tar uttak i samme periode
        var frigitteDager = frigitteDager();
        return flerbarnsdager
                .subtract(forbruktSøker)
                .subtract(forbruktAnnenpart)
                .add(frigitteDager);
    }

    Trekkdager getMaxDagerFlerbarnsdager() {
        return Optional.ofNullable(flerbarnsdager).orElse(Trekkdager.ZERO);
    }

    private Trekkdager forbruktSøker(AktivitetIdentifikator aktivitet, List<FastsattUttakPeriode> søkersPerioder) {

        var perioderMedFlerbarnsdager =
                søkersPerioder.stream().filter(this::trekkFlerbarnsdager).toList();
        return ForbruksTeller.forbruksTeller(
                null,
                aktivitet,
                perioderMedFlerbarnsdager,
                p -> !p.isOpphold(),
                (s, p) -> Trekkdager.ZERO,
                (p, a) -> trekkFlerbarnsdager(p));
    }

    private Trekkdager minForbruktAnnenpart() {
        Map<AktivitetIdentifikator, Trekkdager> forbrukte = new HashMap<>();
        var annenpartsPerioderMedFlerbarnsdager =
                annenpartsPerioder.stream().filter(this::trekkFlerbarnsdager).toList();
        for (var periode : annenpartsPerioderMedFlerbarnsdager) {
            for (var annenpartAktivitet : aktiviteterIPerioder(annenpartsPerioder)) {
                final Trekkdager trekkdager;
                if (!aktivitetIPeriode(periode, annenpartAktivitet)) {
                    trekkdager = minForbrukteDager(periode);
                } else {
                    trekkdager = dagerForUttaksperiode(annenpartAktivitet, periode);
                }

                forbrukte.put(
                        annenpartAktivitet,
                        forbrukte
                                .getOrDefault(annenpartAktivitet, Trekkdager.ZERO)
                                .add(trekkdager));
            }
        }
        return forbrukte.values().stream().min(Trekkdager::compareTo).orElse(Trekkdager.ZERO);
    }

    private boolean trekkFlerbarnsdager(FastsattUttakPeriode periode) {
        return periode.isFlerbarnsdager() && (periode.isForbrukMinsterett() || minsterettDager.equals(Trekkdager.ZERO));
    }

    private Trekkdager minForbrukteDager(FastsattUttakPeriode periode) {
        if (!trekkFlerbarnsdager(periode)) {
            return Trekkdager.ZERO;
        }
        return periode.getAktiviteter().stream()
                .map(FastsattUttakPeriodeAktivitet::getTrekkdager)
                .min(Trekkdager::compareTo)
                .orElse(Trekkdager.ZERO);
    }

    private Trekkdager frigitteDager() {
        Trekkdager sum = Trekkdager.ZERO;
        for (var periode : søkersPerioder) {
            if (trekkFlerbarnsdager(periode)) {
                var overlappendePerioderMedFlerbarnsdager = overlappendePeriode(periode, annenpartsPerioder).stream()
                        .filter(this::trekkFlerbarnsdager)
                        .toList();
                for (var overlappendePeriode : overlappendePerioderMedFlerbarnsdager) {
                    if (innvilgetMedTrekkdager(periode)) {
                        sum = sum.add(frigitteDager(periode, overlappendePeriode));
                    }
                }
            }
        }
        return sum;
    }

    private Trekkdager dagerForUttaksperiode(AktivitetIdentifikator aktivitet, FastsattUttakPeriode periode) {
        return periode.getAktiviteter().stream()
                .filter(a -> a.getAktivitetIdentifikator().equals(aktivitet) && trekkFlerbarnsdager(periode))
                .map(FastsattUttakPeriodeAktivitet::getTrekkdager)
                .findFirst()
                .orElse(Trekkdager.ZERO);
    }

    private Trekkdager frigitteDager(FastsattUttakPeriode periode, FastsattUttakPeriode overlappende) {
        var flerbarnsdagerOverlappendePeriode = minForbrukteDager(overlappende);
        var flerbarnsdagerPeriode = minForbrukteDager(periode);
        // Forenkling: Mulig vi trenger utvidelse her, kan bli feil hvis begge graderer
        // flerbarnsdager
        var min = flerbarnsdagerPeriode.compareTo(flerbarnsdagerOverlappendePeriode) > 0
                ? flerbarnsdagerPeriode
                : flerbarnsdagerOverlappendePeriode;
        return trekkDagerFraDelAvPeriode(
                periode.getFom(), periode.getTom(), overlappende.getFom(), overlappende.getTom(), min);
    }
}
