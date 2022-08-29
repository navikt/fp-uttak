package no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

@RuleDocumentationGrunnlag
public class BeregnKontoerGrunnlag {

    private int antallBarn;
    private boolean morRett;
    private boolean farRett;
    private Dekningsgrad dekningsgrad;
    private boolean farAleneomsorg;
    private boolean morAleneomsorg;
    private boolean annenpartStønadsrettEØS;
    private LocalDate fødselsdato;
    private LocalDate termindato;
    //adopsjon
    private LocalDate omsorgsovertakelseDato;
    private boolean minsterett = true;

    private BeregnKontoerGrunnlag() {
    }

    public int getAntallBarn() {
        return antallBarn;
    }

    public boolean isMorRett() {
        return morRett;
    }

    public boolean isFarRett() {
        return farRett;
    }

    public Dekningsgrad getDekningsgrad() {
        return dekningsgrad;
    }

    public boolean isFarAleneomsorg() {
        return farAleneomsorg;
    }

    public boolean isMorAleneomsorg() {
        return morAleneomsorg;
    }

    public boolean isAnnenpartStønadsrettEØS() {
        return annenpartStønadsrettEØS;
    }

    public boolean erFødsel() {
        return fødselsdato != null || termindato != null;
    }

    public boolean isMinsterett() {
        return minsterett;
    }

    public Optional<LocalDate> getFødselsdato() {
        return Optional.ofNullable(fødselsdato);
    }

    public Optional<LocalDate> getTermindato() {
        return Optional.ofNullable(termindato);
    }

    public LocalDate getFamiliehendelsesdato() {
        if (omsorgsovertakelseDato != null) {
            return omsorgsovertakelseDato;
        }
        var fd = getFødselsdato();
        var td = getTermindato().orElse(null);
        return fd.orElse(td);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final BeregnKontoerGrunnlag kladd = new BeregnKontoerGrunnlag();

        public Builder antallBarn(int antallBarn) {
            kladd.antallBarn = antallBarn;
            return this;
        }

        public Builder morRett(boolean morHarRett) {
            kladd.morRett = morHarRett;
            return this;
        }

        public Builder farRett(boolean farHarRett) {
            kladd.farRett = farHarRett;
            return this;
        }

        public Builder annenpartStønadsrettEØS(boolean annenpartHarStønadsrettEØS) {
            kladd.annenpartStønadsrettEØS = annenpartHarStønadsrettEØS;
            return this;
        }

        public Builder dekningsgrad(Dekningsgrad dekningsgrad) {
            kladd.dekningsgrad = dekningsgrad;
            return this;
        }

        public Builder farAleneomsorg(boolean farAleneomsorg) {
            kladd.farAleneomsorg = farAleneomsorg;
            return this;
        }

        public Builder morAleneomsorg(boolean morAleneomsorg) {
            kladd.morAleneomsorg = morAleneomsorg;
            return this;
        }

        public Builder fødselsdato(LocalDate dato) {
            kladd.fødselsdato = dato;
            return this;
        }

        public Builder termindato(LocalDate dato) {
            kladd.termindato = dato;
            return this;
        }

        public Builder omsorgsovertakelseDato(LocalDate dato) {
            kladd.omsorgsovertakelseDato = dato;
            return this;
        }

        public Builder minsterett(boolean minsterett) {
            kladd.minsterett = minsterett;
            return this;
        }

        public BeregnKontoerGrunnlag build() {
            if (kladd.fødselsdato == null && kladd.termindato == null && kladd.omsorgsovertakelseDato == null) {
                throw new IllegalArgumentException("Forventer minst en familiehendelsedato");
            }
            if (kladd.fødselsdato == null && kladd.termindato == null && kladd.erFødsel()) {
                throw new IllegalArgumentException("Forventer minst en fødselsdato eller termindato");
            }
            if (kladd.omsorgsovertakelseDato == null && !kladd.erFødsel()) {
                throw new IllegalArgumentException("Forventer omsorgsovertakelseDato ved adopsjon");
            }
            return kladd;
        }
    }
}
