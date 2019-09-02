package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class Søknad {

    private List<UttakPeriode> uttaksperioder = new ArrayList<>();
    private Dokumentasjon dokumentasjon = new Dokumentasjon.Builder().build();
    private Søknadstype type = Søknadstype.FØDSEL;

    private Søknad() {
    }

    public List<UttakPeriode> getUttaksperioder() {
        return uttaksperioder.stream().sorted(Comparator.comparing(UttakPeriode::getFom)).collect(Collectors.toList());
    }

    public Dokumentasjon getDokumentasjon() {
        return dokumentasjon;
    }

    public Søknadstype getType() {
        return type;
    }

    public static class Builder {

        private final Søknad kladd = new Søknad();

        public Builder leggTilSøknadsperiode(UttakPeriode uttakPeriode) {
            kladd.uttaksperioder.add(uttakPeriode);
            return this;
        }
        public Builder medSøknadsperioder(List<UttakPeriode> uttakPerioder) {
            kladd.uttaksperioder = uttakPerioder;
            return this;
        }
        public Builder medDokumentasjon(Dokumentasjon dokumentasjon) {
            kladd.dokumentasjon = dokumentasjon;
            return this;
        }
        public Builder medType(Søknadstype type) {
            kladd.type = type;
            return this;
        }

        public Søknad build() {
            return kladd;
        }
    }
}
