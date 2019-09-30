package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class AnnenPart {

    private List<AnnenpartUttaksperiode> uttaksperioder = new ArrayList<>();

    private AnnenPart() {
    }

    public List<AnnenpartUttaksperiode> getUttaksperioder() {
        return uttaksperioder;
    }

    public List<AktivitetIdentifikator> getAktiviteter() {
        return uttaksperioder.stream().flatMap(periode -> periode.getUttakPeriodeAktiviteter().stream())
                .map(UttakPeriodeAktivitet::getAktivitetIdentifikator)
                .distinct()
                .collect(Collectors.toList());
    }

    public static class Builder {

        private final AnnenPart kladd = new AnnenPart();

        public Builder leggTilUttaksperiode(AnnenpartUttaksperiode uttaksperiode) {
            kladd.uttaksperioder.add(uttaksperiode);
            return this;
        }

        public Builder medUttaksperioder(List<AnnenpartUttaksperiode> uttaksperioder) {
            kladd.uttaksperioder = uttaksperioder;
            return this;
        }

        public AnnenPart build() {
            return kladd;
        }
    }
}
