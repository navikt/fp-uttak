package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

class KonfigurasjonBuilder {

    private final Map<Parametertype, Collection<Parameter>> parameterMap;

    private KonfigurasjonBuilder() {
        this.parameterMap = new EnumMap<>(Parametertype.class);
    }

    public static KonfigurasjonBuilder create() {
        return new KonfigurasjonBuilder();
    }

    KonfigurasjonBuilder leggTilParameter(Parametertype parametertype, LocalDate fom, LocalDate tom, Integer verdi) {
        var nyParameter = new Parameter(fom, tom, verdi);
        var parameterListe = parameterMap.get(parametertype);
        if (parameterListe == null) {
            Collection<Parameter> coll = new ArrayList<>();
            coll.add(nyParameter);
            parameterMap.put(parametertype, coll);
        } else {
            var overlapp = parameterListe.stream().anyMatch(nyParameter::overlapper);
            if (overlapp) {
                throw new IllegalArgumentException("Overlappende perioder kan ikke eksistere i konfigurasjon.");
            }
            parameterListe.add(nyParameter);
        }
        return this;
    }

    public Konfigurasjon build() {
        return new Konfigurasjon(parameterMap);
    }
}
