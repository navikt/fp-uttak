package no.nav.foreldrepenger.regler.uttak.konfig;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

public class ParameterVerdier {
    private final Parametertype parametertype;
    private final Collection<Parameter> verdier;

    public ParameterVerdier(Parametertype parametertype, Collection<Parameter> verdier) {
        this.parametertype = parametertype;
        this.verdier = verdier;
    }

    public Integer getParameter(LocalDate dato) {
        return verdier.stream().filter(p -> p.overlapper(dato)).findFirst()
                .map(Parameter::getVerdi)
                .orElseThrow(() -> new IllegalArgumentException("Ingen parameter funnet for " + parametertype.name() + " på dato " + dato));
    }

    public Optional<Integer> getParameterHvisAktivVed(LocalDate dato) {
        return verdier.stream().filter(p -> p.overlapper(dato)).findFirst().map(Parameter::getVerdi);
    }

}
