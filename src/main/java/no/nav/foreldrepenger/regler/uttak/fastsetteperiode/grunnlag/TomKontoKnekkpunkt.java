package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;

public class TomKontoKnekkpunkt {

    private final LocalDate dato;

    public TomKontoKnekkpunkt(LocalDate dato) {
        this.dato = dato;
    }

    public LocalDate getDato() {
        return dato;
    }
}
