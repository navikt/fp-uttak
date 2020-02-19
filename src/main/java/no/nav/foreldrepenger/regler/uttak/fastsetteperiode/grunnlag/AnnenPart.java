package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class AnnenPart {

    private List<AnnenpartUttakPeriode> uttaksperioder = new ArrayList<>();

    private AnnenPart() {
    }

    public List<AnnenpartUttakPeriode> getUttaksperioder() {
        return uttaksperioder;
    }

    public Set<AktivitetIdentifikator> getAktiviteter() {
        return uttaksperioder.stream().flatMap(periode -> periode.getAktiviteter().stream())
                .map(AnnenpartUttakPeriodeAktivitet::getAktivitetIdentifikator)
                .collect(Collectors.toSet());
    }

    public Optional<LocalDate> sisteUttaksdag() {
        var sisteInnvilgetPeriode = uttaksperioder.stream().filter(p -> p.isInnvilget() || p.harTrekkdager() || p.harUtbetaling())
                .min((o1, o2) -> o2.getTom().compareTo(o1.getTom()));
        return sisteInnvilgetPeriode.map(p -> p.getTom());
    }

    public static class Builder {

        private final AnnenPart kladd = new AnnenPart();

        public Builder leggTilUttaksperiode(AnnenpartUttakPeriode uttaksperiode) {
            kladd.uttaksperioder.add(uttaksperiode);
            return this;
        }

        public Builder medUttaksperioder(List<AnnenpartUttakPeriode> uttaksperioder) {
            kladd.uttaksperioder = uttaksperioder;
            return this;
        }

        public AnnenPart build() {
            return kladd;
        }
    }
}
