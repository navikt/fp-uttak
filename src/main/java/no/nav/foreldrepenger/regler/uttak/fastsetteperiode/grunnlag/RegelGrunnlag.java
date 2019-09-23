package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class RegelGrunnlag {

    private Søknad søknad;
    private Behandling behandling;
    private Datoer datoer;
    private RettOgOmsorg rettOgOmsorg;
    private ArbeidGrunnlag arbeid;
    private Revurdering revurdering;
    private AnnenPart annenPart;
    private Map<AktivitetIdentifikator, Kontoer> kontoer = new HashMap<>();
    private Medlemskap medlemskap;
    private Inngangsvilkår inngangsvilkår;
    private Opptjening opptjening;
    private Adopsjon adopsjon;

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

    public ArbeidGrunnlag getArbeid() {
        return arbeid;
    }

    public Revurdering getRevurdering() {
        return revurdering;
    }

    public Set<Stønadskontotype> getGyldigeStønadskontotyper() {
        Set<Stønadskontotype> gyldige = new HashSet<>();
        kontoer.values().forEach(k -> gyldige.addAll(k.getKontoList().stream().map(Konto::getType).collect(Collectors.toList())));
        return gyldige;
    }

    public boolean erRevurdering() {
        return behandling.erRevurdering();
    }

    public AnnenPart getAnnenPart() {
        return annenPart;
    }

    public Map<AktivitetIdentifikator, Kontoer> getKontoer() {
        return kontoer;
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

    public static class Builder  {

        private RegelGrunnlag kladd = new RegelGrunnlag();

        public Builder medSøknad(Søknad søknad) {
            kladd.søknad = søknad;
            return this;
        }
        public Builder medBehandling(Behandling behandling) {
            kladd.behandling = behandling;
            return this;
        }
        public Builder medDatoer(Datoer datoer) {
            kladd.datoer = datoer;
            return this;
        }
        public Builder medRettOgOmsorg(RettOgOmsorg rettOgOmsorg) {
            kladd.rettOgOmsorg = rettOgOmsorg;
            return this;
        }
        public Builder medArbeid(ArbeidGrunnlag arbeid) {
            kladd.arbeid = arbeid;
            return this;
        }
        public Builder medRevurdering(Revurdering revurdering) {
            kladd.revurdering = revurdering;
            return this;
        }
        public Builder medAnnenPart(AnnenPart annenPart) {
            kladd.annenPart = annenPart;
            return this;
        }
        public Builder leggTilKontoer(AktivitetIdentifikator aktivitetIdentifikator, Kontoer kontoer) {
            kladd.kontoer.put(aktivitetIdentifikator, kontoer);
            return this;
        }
        public Builder medMedlemskap(Medlemskap medlemskap) {
            kladd.medlemskap = medlemskap;
            return this;
        }
        public Builder medKontoer(Map<AktivitetIdentifikator, Kontoer> kontoer) {
            kladd.kontoer = kontoer;
            return this;
        }
        public Builder medInngangsvilkår(Inngangsvilkår inngangsvilkår) {
            kladd.inngangsvilkår = inngangsvilkår;
            return this;
        }
        public Builder medOpptjening(Opptjening opptjening) {
            kladd.opptjening = opptjening;
            return this;
        }

        public Builder medAdopsjon(Adopsjon adopsjon) {
            kladd.adopsjon = adopsjon;
            return this;
        }

        public RegelGrunnlag build() {
            if (kladd.getDatoer() != null) {
                validerDatoerOppMotSøknad();
            }
            sjekkAtAlleArbeidsforholdHarKontoer();
            //Hindre gjenbruk
            RegelGrunnlag regelGrunnlag = this.kladd;
            kladd = null;
            return regelGrunnlag;
        }

        private void sjekkAtAlleArbeidsforholdHarKontoer() {
            Set<AktivitetIdentifikator> arbeidsforhold = new HashSet<>(kladd.getArbeid().getAktiviteter());
            Set<AktivitetIdentifikator> arbeidsforholdMedKontoer = kladd.getKontoer().keySet();
            if (!arbeidsforhold.equals(arbeidsforholdMedKontoer)) {
                throw new IllegalArgumentException("Alle arbeidsforhold må ha kontoer");
            }
        }

        private void validerDatoerOppMotSøknad() {
            if (Søknadstype.FØDSEL.equals(kladd.getSøknad().getType())) {
                if (kladd.getDatoer().getFødsel() == null && kladd.getDatoer().getTermin() == null) {
                    throw new IllegalStateException("Forventer enten fødselsdato eller termindato eller begge ved fødselssøknad");
                }
            }
            if (Søknadstype.ADOPSJON.equals(kladd.getSøknad().getType())) {
                if (kladd.getDatoer().getOmsorgsovertakelse() == null) {
                    throw new IllegalStateException("Forventer omsorgsovertakelsedato ved adopsjonssøknad");
                }
            }
        }
    }
}
