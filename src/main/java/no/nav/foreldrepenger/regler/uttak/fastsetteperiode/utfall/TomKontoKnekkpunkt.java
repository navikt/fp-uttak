package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall;

import java.time.LocalDate;

public class TomKontoKnekkpunkt {

    private final LocalDate dato;

    public TomKontoKnekkpunkt(LocalDate dato) {
        this.dato = dato;
    }

    public LocalDate getDato() {
        return dato;
    }

    @Override
    public String toString() {
        return "TomKontoKnekkpunkt{" + "dato=" + dato + '}';
    }
}
