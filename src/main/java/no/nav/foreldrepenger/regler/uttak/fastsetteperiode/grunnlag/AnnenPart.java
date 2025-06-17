package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class AnnenPart {

    private List<AnnenpartUttakPeriode> uttaksperioder = new ArrayList<>();
    private LocalDateTime sisteSøknadMottattTidspunkt;
    private AktivitetskravGrunnlag aktivitetskravGrunnlag;


    private AnnenPart() {
    }

    public List<AnnenpartUttakPeriode> getUttaksperioder() {
        return uttaksperioder;
    }

    public Optional<LocalDate> sisteUttaksdag() {
        var sisteInnvilgetPeriode = uttaksperioder.stream()
            .filter(p -> p.isInnvilget() || p.harTrekkdager() || p.harUtbetaling())
            .min((o1, o2) -> o2.getTom().compareTo(o1.getTom()));
        return sisteInnvilgetPeriode.map(Periode::getTom);
    }

    public LocalDateTime getSisteSøknadMottattTidspunkt() {
        return sisteSøknadMottattTidspunkt;
    }

    public Optional<AktivitetskravGrunnlag> getAktivitetskravGrunnlag() {
        return Optional.ofNullable(aktivitetskravGrunnlag);
    }

    public static class Builder {

        private final AnnenPart kladd = new AnnenPart();

        public Builder uttaksperiode(AnnenpartUttakPeriode uttaksperiode) {
            kladd.uttaksperioder.add(uttaksperiode);
            return this;
        }

        public Builder uttaksperioder(List<AnnenpartUttakPeriode> uttaksperioder) {
            kladd.uttaksperioder = uttaksperioder;
            return this;
        }

        public Builder sisteSøknadMottattTidspunkt(LocalDateTime sisteSøknadMottattTidspunkt) {
            kladd.sisteSøknadMottattTidspunkt = sisteSøknadMottattTidspunkt;
            return this;
        }

        public Builder aktivitetskravGrunnlag(AktivitetskravGrunnlag aktivitetskravGrunnlag) {
            kladd.aktivitetskravGrunnlag = aktivitetskravGrunnlag;
            return this;
        }

        public AnnenPart build() {
            return kladd;
        }
    }
}
