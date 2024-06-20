package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.Set;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ytelser.Ytelser;

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
    private Ytelser ytelser;

    private RegelGrunnlag() {}

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
        return kontoer.getStønadskontotyper();
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

    public Ytelser getYtelser() {
        return ytelser;
    }

    public static class Builder {

        private RegelGrunnlag kladd = new RegelGrunnlag();

        public Builder søknad(Søknad.Builder søknad) {
            kladd.søknad = søknad.build();
            return this;
        }

        public Builder behandling(Behandling.Builder behandling) {
            kladd.behandling = behandling == null ? null : behandling.build();
            return this;
        }

        public Builder datoer(Datoer.Builder datoer) {
            kladd.datoer = datoer == null ? null : datoer.build();
            return this;
        }

        public Builder rettOgOmsorg(RettOgOmsorg.Builder rettOgOmsorg) {
            kladd.rettOgOmsorg = rettOgOmsorg == null ? null : rettOgOmsorg.build();
            return this;
        }

        public Builder arbeid(Arbeid.Builder arbeid) {
            kladd.arbeid = arbeid == null ? null : arbeid.build();
            return this;
        }

        public Builder revurdering(Revurdering.Builder revurdering) {
            kladd.revurdering = revurdering == null ? null : revurdering.build();
            return this;
        }

        public Builder annenPart(AnnenPart.Builder annenPart) {
            kladd.annenPart = annenPart == null ? null : annenPart.build();
            return this;
        }

        public Builder medlemskap(Medlemskap.Builder medlemskap) {
            kladd.medlemskap = medlemskap == null ? null : medlemskap.build();
            return this;
        }

        public Builder inngangsvilkår(Inngangsvilkår.Builder inngangsvilkår) {
            kladd.inngangsvilkår = inngangsvilkår == null ? null : inngangsvilkår.build();
            return this;
        }

        public Builder opptjening(Opptjening.Builder opptjening) {
            kladd.opptjening = opptjening == null ? null : opptjening.build();
            return this;
        }

        public Builder adopsjon(Adopsjon.Builder adopsjon) {
            kladd.adopsjon = adopsjon == null ? null : adopsjon.build();
            return this;
        }

        public Builder kontoer(Kontoer.Builder kontoer) {
            kladd.kontoer = kontoer == null ? null : kontoer.build();
            return this;
        }

        public Builder ytelser(Ytelser ytelser) {
            kladd.ytelser = ytelser;
            return this;
        }

        public RegelGrunnlag build() {
            if (kladd.getDatoer() != null) {
                validerDatoerOppMotSøknad();
            }
            if (kladd.ytelser == null) {
                kladd.ytelser = new Ytelser(null);
            }
            // Hindre gjenbruk
            var regelGrunnlag = this.kladd;
            kladd = null;
            return regelGrunnlag;
        }

        private void validerDatoerOppMotSøknad() {
            var type = kladd.getSøknad().getType();
            var datoer = kladd.getDatoer();

            if (type == Søknadstype.TERMIN && datoer.getTermin() == null) {
                throw new IllegalStateException("Forventer termindato ved terminsøknad");
            }
            if (type == Søknadstype.FØDSEL && datoer.getFødsel() == null && datoer.getTermin() == null) {
                throw new IllegalStateException("Forventer fødselsdato eller termindato eller begge ved fødselsøknad");
            }
            if (type == Søknadstype.ADOPSJON && datoer.getOmsorgsovertakelse() == null) {
                throw new IllegalStateException("Forventer omsorgsovertakelsesdato ved adopsjonssøknad");
            }
        }
    }
}
