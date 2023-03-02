package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;


import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Kontoer {

    private Map<Stønadskontotype, Integer> stønadskonti = new EnumMap<>(Stønadskontotype.class);
    private Map<Spesialkontotype, Integer> spesialkonti = new EnumMap<>(Spesialkontotype.class);

    private Kontoer() {

    }

    public Set<Stønadskontotype> getStønadskontotyper() {
        return stønadskonti.keySet();
    }

    public boolean harStønadskonto(Stønadskontotype stønadskontotype) {
        return stønadskonti.containsKey(stønadskontotype);
    }

    public Integer getStønadskontoTrekkdager(Stønadskontotype stønadskontotype) {
        return stønadskonti.get(stønadskontotype);
    }

    public Set<Spesialkontotype> getSpesialkontotyper() {
        return spesialkonti.keySet();
    }

    public boolean harSpesialkonto(Spesialkontotype delkontotype) {
        return spesialkonti.containsKey(delkontotype);
    }

    public Integer getSpesialkontoTrekkdager(Spesialkontotype delkontotype) {
        return spesialkonti.get(delkontotype);
    }

    public static class Builder {

        private int utenAktivitetskravDager = 0;
        private int minsterettDager = 0;

        private final Kontoer kladd = new Kontoer();

        public Builder konto(Stønadskontotype konto, int trekkdager) {
            kladd.stønadskonti.put(konto, trekkdager);
            return this;
        }

        public Builder konto(Konto.Builder konto) {
            var k = konto.build();
            kladd.stønadskonti.put(k.getType(), k.getTrekkdager());
            return this;
        }

        public Builder kontoList(List<Konto.Builder> kontoList) {
            kontoList.stream().map(Konto.Builder::build).forEach(k -> kladd.stønadskonti.put(k.getType(), k.getTrekkdager()));
            return this;
        }

        public Builder spesialkonto(Spesialkontotype spesialkontotype, int trekkdager) {
            kladd.spesialkonti.put(spesialkontotype, trekkdager);
            return this;
        }

        public Builder farUttakRundtFødselDager(int farUttakRundtFødselDager) {
            kladd.spesialkonti.put(Spesialkontotype.FAR_RUNDT_FØDSEL, farUttakRundtFødselDager);
            return this;
        }

        public Builder minsterettDager(int minsterettDager) {
            this.minsterettDager = minsterettDager;
            kladd.spesialkonti.put(Spesialkontotype.BARE_FAR_MINSTERETT, minsterettDager);
            return this;
        }

        public Builder etterNesteStønadsperiodeDager(int etterNesteSakDager) {
            kladd.spesialkonti.put(Spesialkontotype.TETTE_FØDSLER, etterNesteSakDager);
            return this;
        }

        public Builder utenAktivitetskravDager(int utenAktivitetskravDager) {
            this.utenAktivitetskravDager = utenAktivitetskravDager;
            kladd.spesialkonti.put(Spesialkontotype.UTEN_AKTIVITETSKRAV, utenAktivitetskravDager);
            return this;
        }

        public Builder flerbarnsdager(int flerbarnsdager) {
            kladd.spesialkonti.put(Spesialkontotype.FLERBARN, flerbarnsdager);
            return this;
        }

        public Kontoer build() {
            if (this.minsterettDager > 0 && this.utenAktivitetskravDager > 0) {
                throw new IllegalArgumentException("Utviklerfeil: Sak med både minsterett og dager uten aktivitetskrav");
            }
            return kladd;
        }
    }
}
