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

    public SaldoUtregningFlerbarnsdager(List<FastsattUttakPeriode> søkersPerioder,
                                        List<FastsattUttakPeriode> annenpartsPerioder,
                                        Set<AktivitetIdentifikator> søkersAktiviteter,
                                        Trekkdager flerbarnsdager) {

        this.søkersPerioder = søkersPerioder;
        this.annenpartsPerioder = annenpartsPerioder;
        this.søkersAktiviteter = søkersAktiviteter;
        this.flerbarnsdager = flerbarnsdager;
    }

    Trekkdager restSaldo() {
        return søkersAktiviteter.stream()
                .map(p -> restSaldo(p))
                .max(Trekkdager::compareTo)
                .orElse(Trekkdager.ZERO);
    }

    Trekkdager restSaldo(AktivitetIdentifikator aktivitet) {
        var forbruktSøker = forbruktSøker(aktivitet, søkersPerioder);
        var forbruktAnnenpart = minForbruktAnnenpart();
        //frigitte dager er dager fra annenpart som blir ledig når søker tar uttak i samme periode
        var frigitteDager = frigitteDager();
        return flerbarnsdager
                .subtract(forbruktSøker)
                .subtract(forbruktAnnenpart)
                .add(frigitteDager);
    }

    Trekkdager getMaxDagerFlerbarnsdager() {
        return Optional.ofNullable(flerbarnsdager).orElse(Trekkdager.ZERO);
    }


    private Trekkdager forbruktSøker(AktivitetIdentifikator aktivitet,
                                     List<FastsattUttakPeriode> søkersPerioder) {

       var perioderMedFlerbarnsdager = søkersPerioder.stream().filter(p -> p.isFlerbarnsdager()).toList();
        return ForbruksTeller.forbruksTeller(null, aktivitet, perioderMedFlerbarnsdager,
                FastsattUttakPeriode::isOpphold, (s,p) -> Trekkdager.ZERO,
                (p, a) -> p.isFlerbarnsdager());
    }

    private Trekkdager minForbruktAnnenpart() {
        Map<AktivitetIdentifikator, Trekkdager> forbrukte = new HashMap<>();
        var annenpartsPerioderMedFlerbarnsdager = annenpartsPerioder.stream()
                .filter(ap -> ap.isFlerbarnsdager())
                .toList();
        for (var periode : annenpartsPerioderMedFlerbarnsdager) {
            for (var annenpartAktivitet : aktiviteterIPerioder(annenpartsPerioder)) {
                final Trekkdager trekkdager;
                if (!aktivitetIPeriode(periode, annenpartAktivitet)) {
                    trekkdager = minForbrukteDager(periode);
                } else {
                    trekkdager = dagerForUttaksperiode(annenpartAktivitet, periode);
                }

                forbrukte.put(annenpartAktivitet, forbrukte.getOrDefault(annenpartAktivitet, Trekkdager.ZERO).add(trekkdager));
            }
        }
        return forbrukte.values().stream().min((o1, o2) -> o1.compareTo(o2)).orElse(Trekkdager.ZERO);
    }

    private Trekkdager minForbrukteDager(FastsattUttakPeriode periode) {
        if (!periode.isFlerbarnsdager()) {
            return Trekkdager.ZERO;
        }
        Trekkdager minForbrukt = null;
        for (var aktivitet : periode.getAktiviteter()) {
            if (minForbrukt == null || minForbrukt.compareTo(aktivitet.getTrekkdager()) > 0) {
                minForbrukt = aktivitet.getTrekkdager();
            }
        }
        return minForbrukt == null ? Trekkdager.ZERO : minForbrukt;
    }

    private Trekkdager frigitteDager() {
        Trekkdager sum = Trekkdager.ZERO;
        for (var periode : søkersPerioder) {
            if (periode.isFlerbarnsdager()) {
                var overlappendePerioderMedFlerbarnsdager = overlappendePeriode(periode, annenpartsPerioder)
                        .stream()
                        .filter(op -> op.isFlerbarnsdager())
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
        for (var periodeAktivitet : periode.getAktiviteter()) {
            if (periodeAktivitet.getAktivitetIdentifikator().equals(aktivitet) && periode.isFlerbarnsdager()) {
                return periodeAktivitet.getTrekkdager();
            }
        }
        return Trekkdager.ZERO;
    }

    private Trekkdager frigitteDager(FastsattUttakPeriode periode,
                              FastsattUttakPeriode overlappende) {
        var flerbarnsdagerOverlappendePeriode = minTrekkdager(overlappende);
        var flerbarnsdagerPeriode = minTrekkdager(periode);
        //Forenkling: Mulig vi trenger utvidelse her, kan bli feil hvis begge graderer flerbarnsdager
        var min = flerbarnsdagerPeriode.compareTo(flerbarnsdagerOverlappendePeriode) > 0 ?
                flerbarnsdagerPeriode : flerbarnsdagerOverlappendePeriode;
        return trekkDagerFraDelAvPeriode(periode.getFom(), periode.getTom(), overlappende.getFom(), overlappende.getTom(), min);
    }

    private Trekkdager minTrekkdager(FastsattUttakPeriode periode) {
        if (!periode.isFlerbarnsdager()) {
            return Trekkdager.ZERO;
        }
        FastsattUttakPeriodeAktivitet aktivitetMedMinstTrekkdager = null;
        for (var aktivitet : periode.getAktiviteter()) {
            if (aktivitetMedMinstTrekkdager == null || aktivitet.getTrekkdager().compareTo(aktivitetMedMinstTrekkdager.getTrekkdager()) < 0) {
                aktivitetMedMinstTrekkdager = aktivitet;
            }
        }
        return aktivitetMedMinstTrekkdager == null ? Trekkdager.ZERO : aktivitetMedMinstTrekkdager.getTrekkdager();
    }
}
