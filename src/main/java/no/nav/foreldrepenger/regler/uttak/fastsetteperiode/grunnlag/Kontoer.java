package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;


import java.util.ArrayList;
import java.util.List;

public final class Kontoer {

    private List<Konto> kontoList = new ArrayList<>();

    private Kontoer() {

    }

    public List<Konto> getKontoList() {
        return kontoList;
    }

    public static class Builder {

        private Kontoer kladd = new Kontoer();

        public Builder leggTilKonto(Konto konto) {
            kladd.kontoList.add(konto);
            return this;
        }

        public Builder medKontoList(List<Konto> kontoList) {
            kladd.kontoList = kontoList;
            return this;
        }

        public Kontoer build() {
            return kladd;
        }
    }
}
