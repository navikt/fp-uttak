package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningUtil.aktivitetIPeriode;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningUtil.aktiviteterIPerioder;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningUtil.innvilgetMedTrekkdager;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningUtil.overlappendePeriode;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregningUtil.trekkDagerFraDelAvPeriode;

import java.time.LocalDateTime;
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
    private final boolean berørtBehandling;
    private final Set<AktivitetIdentifikator> søkersAktiviteter;
    private final LocalDateTime sisteSøknadMottattTidspunktSøker;
    private final LocalDateTime sisteSøknadMottattTidspunktAnnenpart;
    private final Trekkdager flerbarnsdager;

    public SaldoUtregningFlerbarnsdager(List<FastsattUttakPeriode> søkersPerioder,
                                        List<FastsattUttakPeriode> annenpartsPerioder,
                                        boolean berørtBehandling,
                                        Set<AktivitetIdentifikator> søkersAktiviteter,
                                        LocalDateTime sisteSøknadMottattTidspunktSøker,
                                        LocalDateTime sisteSøknadMottattTidspunktAnnenpart,
                                        Trekkdager flerbarnsdager) {

        this.søkersPerioder = søkersPerioder;
        this.annenpartsPerioder = annenpartsPerioder;
        this.berørtBehandling = berørtBehandling;
        this.søkersAktiviteter = søkersAktiviteter;
        this.sisteSøknadMottattTidspunktSøker = sisteSøknadMottattTidspunktSøker;
        this.sisteSøknadMottattTidspunktAnnenpart = sisteSøknadMottattTidspunktAnnenpart;
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
        var sum = Trekkdager.ZERO;

        var perioderMedFlerbarnsdager = søkersPerioder.stream().filter(p -> p.isFlerbarnsdager()).toList();
        for (var i = 0; i < perioderMedFlerbarnsdager.size(); i++) {
            var periodeSøker = perioderMedFlerbarnsdager.get(i);
            var nestePeriodeSomIkkeErOpphold = SaldoUtregningUtil.nestePeriodeSomIkkeErOpphold(perioderMedFlerbarnsdager, i);
            if (!aktivitetIPeriode(periodeSøker, aktivitet) &&
                    (nestePeriodeSomIkkeErOpphold.isEmpty() || aktivitetIPeriode(nestePeriodeSomIkkeErOpphold.get(), aktivitet))) {
                var perioderTomPeriode = perioderMedFlerbarnsdager.subList(0, i + 1);
                var eksisterendeAktiviteter = aktiviteterIPerioder(perioderTomPeriode);
                eksisterendeAktiviteter.remove(aktivitet);
                var minForbrukteDagerEksisterendeAktiviteter = eksisterendeAktiviteter.stream()
                        .map(a -> forbruktSøker(a, perioderTomPeriode))
                        .min(Trekkdager::compareTo)
                        .orElseThrow();
                sum = sum.add(minForbrukteDagerEksisterendeAktiviteter);
            } else {
                sum = sum.add(dagerForUttaksperiode(aktivitet, periodeSøker));
            }
        }

        return sum;
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
        var sum = 0;
        for (var periode : søkersPerioder) {
            var overlappendePerioderMedFlerbarnsdager = overlappendePeriode(periode, annenpartsPerioder)
                    .stream()
                    .filter(op -> op.isFlerbarnsdager())
                    .toList();
            for (var overlappendePeriode : overlappendePerioderMedFlerbarnsdager) {
                if (!tapendePeriode(periode, overlappendePeriode) && innvilgetMedTrekkdager(periode)) {
                    sum += frigitteDager(periode, overlappendePeriode);
                }
            }
        }
        return new Trekkdager(sum);
    }

    private boolean tapendePeriode(FastsattUttakPeriode periode, FastsattUttakPeriode overlappendePeriode) {
        return SaldoUtregningUtil.tapendePeriode(periode, overlappendePeriode, berørtBehandling, sisteSøknadMottattTidspunktSøker,
                sisteSøknadMottattTidspunktAnnenpart);
    }

    private Trekkdager dagerForUttaksperiode(AktivitetIdentifikator aktivitet, FastsattUttakPeriode periode) {
        for (var periodeAktivitet : periode.getAktiviteter()) {
            if (periodeAktivitet.getAktivitetIdentifikator().equals(aktivitet) && periode.isFlerbarnsdager()) {
                return periodeAktivitet.getTrekkdager();
            }
        }
        return Trekkdager.ZERO;
    }

    private int frigitteDager(FastsattUttakPeriode periode,
                              FastsattUttakPeriode overlappende) {
        if (!periode.isSamtidigUttak() && !overlappende.isSamtidigUttak() && overlappende.isFlerbarnsdager()) {
            var flerbarnsdagerOverlappendePeriode = minTrekkdager(overlappende);
            return trekkDagerFraDelAvPeriode(periode.getFom(), periode.getTom(), overlappende.getFom(), overlappende.getTom(),
                    flerbarnsdagerOverlappendePeriode.decimalValue());

        }
        return 0;
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
