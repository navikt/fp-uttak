package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class Kontoer {

    private List<Konto> kontoList = new ArrayList<>();

    private Kontoer() {

    }

    public List<Konto> getKontoList() {
        return kontoList;
    }

    public static class Builder {

        private Kontoer kladd = new Kontoer();

        public Builder leggTilKonto(Konto.Builder konto) {
            kladd.kontoList.add(konto.build());
            return this;
        }

        public Builder medKontoList(List<Konto.Builder> kontoList) {
            kladd.kontoList = kontoList.stream().map(Konto.Builder::build).collect(Collectors.toList());
            return this;
        }

        public Kontoer build() {
            return kladd;
        }
    }
}
