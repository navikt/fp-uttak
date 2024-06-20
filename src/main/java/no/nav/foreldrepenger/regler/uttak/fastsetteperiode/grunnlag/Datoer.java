package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;
import java.util.Optional;

public final class Datoer {

    private LocalDate omsorgsovertakelse;
    private LocalDate termin;
    private LocalDate fødsel;
    private Dødsdatoer dødsdatoer;
    // Ikke sleng inn flere datoer her uten å prøve å plassere i andre mer passende klasser
    private LocalDate startdatoNesteStønadsperiode; // Solo-dato

    private Datoer() {}

    public LocalDate getFamiliehendelse() {
        return Optional.ofNullable(omsorgsovertakelse)
                .or(() -> Optional.ofNullable(fødsel))
                .or(() -> Optional.ofNullable(termin))
                .orElseThrow(() -> new IllegalStateException("Ingen familiehendelse"));
    }

    public LocalDate getTermin() {
        return termin;
    }

    public LocalDate getFødsel() {
        return fødsel;
    }

    public LocalDate getOmsorgsovertakelse() {
        return omsorgsovertakelse;
    }

    public Dødsdatoer getDødsdatoer() {
        return dødsdatoer;
    }

    public Optional<LocalDate> getStartdatoNesteStønadsperiode() {
        return Optional.ofNullable(startdatoNesteStønadsperiode);
    }

    public static class Builder {
        private final Datoer kladd = new Datoer();

        public Builder fødsel(LocalDate fødsel) {
            kladd.fødsel = fødsel;
            return this;
        }

        public Builder termin(LocalDate termin) {
            kladd.termin = termin;
            return this;
        }

        public Builder omsorgsovertakelse(LocalDate omsorgsovertakelse) {
            kladd.omsorgsovertakelse = omsorgsovertakelse;
            return this;
        }

        public Builder startdatoNesteStønadsperiode(LocalDate startdatoNesteStønadsperiode) {
            kladd.startdatoNesteStønadsperiode = startdatoNesteStønadsperiode;
            return this;
        }

        public Builder dødsdatoer(Dødsdatoer.Builder dødsdatoer) {
            kladd.dødsdatoer = dødsdatoer == null ? null : dødsdatoer.build();
            return this;
        }

        public Datoer build() {
            return kladd;
        }
    }
}
