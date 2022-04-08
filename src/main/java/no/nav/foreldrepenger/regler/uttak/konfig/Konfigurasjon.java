package no.nav.foreldrepenger.regler.uttak.konfig;

import java.time.LocalDate;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class Konfigurasjon {

    public static final LocalDate PREMATURUKER_REGELENDRING_START_DATO = LocalDate.of(2019, 7, 1);

    private final Map<Parametertype, ParameterVerdier> parameterMap = new EnumMap<>(Parametertype.class);

    Konfigurasjon(Map<Parametertype, Collection<Parameter>> parameterMap) {
        for (var entry : parameterMap.entrySet()) {
            this.parameterMap.put(entry.getKey(), new ParameterVerdier(entry.getKey(), entry.getValue()));
        }
    }

    public int getParameter(Parametertype parametertype, final LocalDate dato) {
        return getParameter(parametertype, Integer.class, dato);
    }

    public Optional<Integer> getParameterHvisAktivVed(Parametertype parametertype, final LocalDate dato) {
        return Optional.ofNullable(this.parameterMap.get(parametertype))
                .flatMap(p -> p.getParameterHvisAktivVed(dato));
    }

    public <T> T getParameter(Parametertype parametertype, Class<T> klasse, final LocalDate dato) {
        return getParameterVerdier(parametertype, klasse).getParameter(dato);
    }

    public <T> ParameterVerdier<T> getParameterVerdier(Parametertype parametertype, Class<T> klasse) {
        if (!parametertype.getKlasseForVerdier().equals(klasse)) {
            throw new IllegalArgumentException("Utvikler-feil: kan ikke spørre etter " + klasse.getName() + " for " + parametertype);
        }
        ParameterVerdier<T> resultat = this.parameterMap.get(parametertype);
        if (resultat == null) {
            throw new IllegalArgumentException("Konfigurasjon-feil/Utvikler-feil: mangler parameter av type " + parametertype);
        }
        return resultat;
    }

}
