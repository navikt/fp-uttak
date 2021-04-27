package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class Søknad {

    private List<OppgittPeriode> oppgittePerioder = new ArrayList<>();
    private Dokumentasjon dokumentasjon = new Dokumentasjon.Builder().build();
    private Søknadstype type = Søknadstype.FØDSEL;
    private LocalDateTime mottattTidspunkt;

    private Søknad() {
    }

    public List<OppgittPeriode> getOppgittePerioder() {
        return oppgittePerioder.stream().sorted(Comparator.comparing(OppgittPeriode::getFom)).collect(Collectors.toList());
    }

    public Dokumentasjon getDokumentasjon() {
        return dokumentasjon;
    }

    public Søknadstype getType() {
        return type;
    }

    public LocalDateTime getMottattTidspunkt() {
        return mottattTidspunkt;
    }

    public static class Builder {

        private final Søknad kladd = new Søknad();

        public Builder leggTilOppgittPeriode(OppgittPeriode oppgittPeriode) {
            kladd.oppgittePerioder.add(oppgittPeriode);
            return this;
        }

        public Builder medOppgittePerioder(List<OppgittPeriode> oppgittPerioder) {
            kladd.oppgittePerioder = oppgittPerioder;
            return this;
        }

        public Builder medDokumentasjon(Dokumentasjon.Builder dokumentasjon) {
            kladd.dokumentasjon = dokumentasjon.build();
            return this;
        }

        public Builder medType(Søknadstype type) {
            kladd.type = type;
            return this;
        }

        public Builder medMottattTidspunkt(LocalDateTime mottattTidspunkt) {
            kladd.mottattTidspunkt = mottattTidspunkt;
            return this;
        }

        public Søknad build() {
            validerOverlapp();
            return kladd;
        }

        private void validerOverlapp() {
            for (var i = 0; i < kladd.oppgittePerioder.size(); i++) {
                for (var j = i + 1; j < kladd.oppgittePerioder.size(); j++) {
                    if (kladd.oppgittePerioder.get(i).overlapper(kladd.oppgittePerioder.get(j))) {
                        throw new IllegalStateException("Overlapp i søknad");
                    }
                }
            }
        }
    }
}
