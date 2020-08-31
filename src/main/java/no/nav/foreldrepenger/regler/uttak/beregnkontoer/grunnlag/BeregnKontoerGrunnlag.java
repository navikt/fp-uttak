package no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

@RuleDocumentationGrunnlag
public class BeregnKontoerGrunnlag {

    private Integer antallLevendeBarn;
    private boolean morRett;
    private boolean farRett;
    private Dekningsgrad dekningsgrad;
    private boolean farAleneomsorg;
    private boolean morAleneomsorg;
    private LocalDate fødselsdato;
    private LocalDate dødsdato;
    private LocalDate termindato;
    //adopsjon
    private LocalDate omsorgsovertakelseDato;

    private BeregnKontoerGrunnlag() {
    }

    public Optional<Integer> getAntallLevendeBarn() {
        return Optional.ofNullable(antallLevendeBarn);
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

    public boolean erFødsel() {
        return fødselsdato != null || termindato != null;
    }

    public Optional<LocalDate> getFødselsdato() {
        return Optional.ofNullable(fødselsdato);
    }

    public Optional<LocalDate> getDødsdato() {
        return Optional.ofNullable(dødsdato);
    }

    public Optional<LocalDate> getTermindato() {
        return Optional.ofNullable(termindato);
    }

    public LocalDate getFamiliehendelsesdato() {
        if (omsorgsovertakelseDato != null) {
            return omsorgsovertakelseDato;
        }
        Optional<LocalDate> fd = getFødselsdato();
        LocalDate td = getTermindato().orElse(null);
        return fd.orElse(td);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BeregnKontoerGrunnlag kladd = new BeregnKontoerGrunnlag();

        public Builder medAntallLevendeBarn(Integer antallLevendeBarn) {
            kladd.antallLevendeBarn = antallLevendeBarn;
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

        public Builder medDekningsgrad(Dekningsgrad dekningsgrad) {
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

        public Builder medFødselsdato(LocalDate dato) {
            kladd.fødselsdato = dato;
            return this;
        }

        public Builder medDødsdato(LocalDate dato) {
            kladd.dødsdato = dato;
            return this;
        }

        public Builder medTermindato(LocalDate dato) {
            kladd.termindato = dato;
            return this;
        }

        public Builder medOmsorgsovertakelseDato(LocalDate dato) {
            kladd.omsorgsovertakelseDato = dato;
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
