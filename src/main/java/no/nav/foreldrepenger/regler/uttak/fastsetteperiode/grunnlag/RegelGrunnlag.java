package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class RegelGrunnlag {

    private Søknad søknad;
    private Behandling behandling;
    private Datoer datoer;
    private RettOgOmsorg rettOgOmsorg;
    private Arbeid arbeid;
    private Revurdering revurdering;
    private AnnenPart annenPart;
    private Medlemskap medlemskap;
    private Inngangsvilkår inngangsvilkår;
    private Opptjening opptjening;
    private Adopsjon adopsjon;
    private Kontoer kontoer;
    private Integer totaltAntallBarnForFødsel;
    private Integer dekningsgrad;

    private RegelGrunnlag() {

    }

    public Søknad getSøknad() {
        return søknad;
    }

    public Behandling getBehandling() {
        return behandling;
    }

    public Datoer getDatoer() {
        return datoer;
    }

    public RettOgOmsorg getRettOgOmsorg() {
        return rettOgOmsorg;
    }

    public Arbeid getArbeid() {
        return arbeid;
    }

    public Revurdering getRevurdering() {
        return revurdering;
    }

    public Set<Stønadskontotype> getGyldigeStønadskontotyper() {
        return kontoer.getKontoList().stream().map(Konto::getType).collect(Collectors.toSet());
    }

    public boolean erRevurdering() {
        return revurdering != null;
    }

    public AnnenPart getAnnenPart() {
        return annenPart;
    }

    public Medlemskap getMedlemskap() {
        return medlemskap;
    }

    public Inngangsvilkår getInngangsvilkår() {
        return inngangsvilkår;
    }

    public Opptjening getOpptjening() {
        return opptjening;
    }

    public Adopsjon getAdopsjon() {
        return adopsjon;
    }

    public Kontoer getKontoer() {
        return kontoer;
    }

    public Optional<Integer> getTotaltAntallBarnForFødsel() {
        return Optional.ofNullable(totaltAntallBarnForFødsel);
    }

    public Optional<Integer> getDekningsgrad() {
        return Optional.ofNullable(dekningsgrad);
    }

    public static class Builder  {

        private RegelGrunnlag kladd = new RegelGrunnlag();

        public Builder medSøknad(Søknad.Builder søknad) {
            kladd.søknad = søknad.build();
            return this;
        }
        public Builder medBehandling(Behandling.Builder behandling) {
            kladd.behandling = behandling == null ? null : behandling.build();
            return this;
        }
        public Builder medDatoer(Datoer.Builder datoer) {
            kladd.datoer = datoer == null ? null : datoer.build();
            return this;
        }
        public Builder medRettOgOmsorg(RettOgOmsorg.Builder rettOgOmsorg) {
            kladd.rettOgOmsorg = rettOgOmsorg == null ? null : rettOgOmsorg.build();
            return this;
        }
        public Builder medArbeid(Arbeid.Builder arbeid) {
            kladd.arbeid = arbeid == null ? null : arbeid.build();
            return this;
        }
        public Builder medRevurdering(Revurdering.Builder revurdering) {
            kladd.revurdering = revurdering == null ? null : revurdering.build();
            return this;
        }
        public Builder medAnnenPart(AnnenPart.Builder annenPart) {
            kladd.annenPart = annenPart == null ? null : annenPart.build();
            return this;
        }

        public Builder medMedlemskap(Medlemskap.Builder medlemskap) {
            kladd.medlemskap = medlemskap == null ? null : medlemskap.build();
            return this;
        }

        public Builder medInngangsvilkår(Inngangsvilkår.Builder inngangsvilkår) {
            kladd.inngangsvilkår = inngangsvilkår == null ? null : inngangsvilkår.build();
            return this;
        }
        public Builder medOpptjening(Opptjening.Builder opptjening) {
            kladd.opptjening = opptjening == null ? null : opptjening.build();
            return this;
        }

        public Builder medAdopsjon(Adopsjon.Builder adopsjon) {
            kladd.adopsjon = adopsjon == null ? null : adopsjon.build();
            return this;
        }

        public Builder medKontoer(Kontoer.Builder kontoer) {
            kladd.kontoer = kontoer == null ? null : kontoer.build();
            return this;
        }

        public Builder medTotaltAntallBarnForFødsel(Integer totaltAntallBarnForFødsel) {
            kladd.totaltAntallBarnForFødsel = totaltAntallBarnForFødsel;
            return this;
        }

        public Builder medDekningsgrad(Integer dekningsgrad) {
            kladd.dekningsgrad = dekningsgrad;
            return this;
        }

        public RegelGrunnlag build() {
            if (kladd.getDatoer() != null) {
                validerDatoerOppMotSøknad();
            }
            //Hindre gjenbruk
            RegelGrunnlag regelGrunnlag = this.kladd;
            kladd = null;
            return regelGrunnlag;
        }

        private void validerDatoerOppMotSøknad() {
            var type = kladd.getSøknad().getType();
            var datoer = kladd.getDatoer();

            if (type == Søknadstype.TERMIN && datoer.getTermin() == null) {
                throw new IllegalStateException("Forventer termindato ved terminsøknad");
            } else if (type == Søknadstype.FØDSEL && datoer.getFødsel() == null && datoer.getTermin() == null) {
                throw new IllegalStateException("Forventer fødselsdato eller termindato eller begge ved fødselsøknad");
            } else if (type == Søknadstype.ADOPSJON && datoer.getOmsorgsovertakelse() == null) {
                throw new IllegalStateException("Forventer omsorgsovertakelsesdato ved adopsjonssøknad");
            }
        }
    }
}
