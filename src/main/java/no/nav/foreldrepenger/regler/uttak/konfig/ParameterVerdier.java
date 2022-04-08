package no.nav.foreldrepenger.regler.uttak.konfig;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

public class ParameterVerdier<T> {
    private final Parametertype parametertype;
    private final Collection<Parameter> verdier;

    public ParameterVerdier(Parametertype parametertype, Collection<Parameter> verdier) {
        this.parametertype = parametertype;
        this.verdier = verdier;
    }

    public T getParameter(LocalDate dato) {
        var optionalParam = verdier.stream().filter(p -> p.overlapper(dato)).findFirst();
        if (optionalParam.isPresent()) {
            return (T) optionalParam.get().getVerdi();
        }
        throw new IllegalArgumentException("Ingen parameter funnet for " + parametertype.name() + " på dato " + dato);
    }

    public Optional<T> getParameterHvisAktivVed(LocalDate dato) {
        return verdier.stream().filter(p -> p.overlapper(dato)).findFirst().map(p -> (T) p.getVerdi());
    }

}
