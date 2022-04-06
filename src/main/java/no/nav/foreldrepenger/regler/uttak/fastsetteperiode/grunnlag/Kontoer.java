package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class Kontoer {

    private List<Konto> kontoList = new ArrayList<>();
    private int minsterettDager = 0;
    private int utenAktivitetskravDager = 0;
    private int flerbarnsdager = 0;

    private Kontoer() {

    }

    public List<Konto> getKontoList() {
        return kontoList;
    }

    public int getMinsterettDager() {
        return minsterettDager;
    }

    public int getUtenAktivitetskravDager() {
        return utenAktivitetskravDager;
    }

    public int getFlerbarnsdager() {
        return flerbarnsdager;
    }

    public static class Builder {

        private final Kontoer kladd = new Kontoer();

        public Builder konto(Konto.Builder konto) {
            kladd.kontoList.add(konto.build());
            return this;
        }

        public Builder kontoList(List<Konto.Builder> kontoList) {
            kladd.kontoList = kontoList.stream().map(Konto.Builder::build).collect(Collectors.toList());
            return this;
        }

        public Builder minsterettDager(int minsterettDager) {
            kladd.minsterettDager = minsterettDager;
            return this;
        }

        public Builder utenAktivitetskravDager(int utenAktivitetskravDager) {
            kladd.utenAktivitetskravDager = utenAktivitetskravDager;
            return this;
        }

        public Builder flerbarnsdager(int flerbarnsdager) {
            kladd.flerbarnsdager = flerbarnsdager;
            return this;
        }

        public Kontoer build() {
            if (kladd.minsterettDager > 0 && kladd.utenAktivitetskravDager > 0) {
                throw new IllegalArgumentException("Utviklerfeil: Sak med både minsterett og dager uten aktivitetskrav");
            }
            return kladd;
        }
    }
}
