package no.nav.foreldrepenger.regler.soknadsfrist.grunnlag;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

@RuleDocumentationGrunnlag
public class SøknadsfristGrunnlag {

    private LocalDate søknadMottattDato;
    private LocalDate førsteUttaksdato;

    public LocalDate getFørsteUttaksdato() {
        return førsteUttaksdato;
    }

    public LocalDate getSøknadMottattDato() {
        return søknadMottattDato;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final SøknadsfristGrunnlag kladd = new SøknadsfristGrunnlag();

        public Builder søknadMottattDato(LocalDate søknadMottattDato) {
            kladd.søknadMottattDato = søknadMottattDato;
            return this;
        }

        public Builder førsteUttaksdato(LocalDate uttaksdato) {
            kladd.førsteUttaksdato = uttaksdato;
            return this;
        }

        public SøknadsfristGrunnlag build() {
            Objects.requireNonNull(kladd.søknadMottattDato);
            return kladd;
        }
    }
}
