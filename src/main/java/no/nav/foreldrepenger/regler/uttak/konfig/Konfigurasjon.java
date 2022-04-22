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

    public Optional<Integer> getParameterHvisAktivVed(Parametertype parametertype, final LocalDate dato) {
        return Optional.ofNullable(this.parameterMap.get(parametertype))
                .flatMap(p -> p.getParameterHvisAktivVed(dato));
    }

    public Integer getParameter(Parametertype parametertype, final LocalDate dato) {
        return getParameterVerdier(parametertype).getParameter(dato);
    }

    public ParameterVerdier getParameterVerdier(Parametertype parametertype) {
        return Optional.ofNullable(this.parameterMap.get(parametertype))
                .orElseThrow(() -> new IllegalArgumentException("Konfigurasjon-feil/Utvikler-feil: mangler parameter av type " + parametertype));
    }

}
