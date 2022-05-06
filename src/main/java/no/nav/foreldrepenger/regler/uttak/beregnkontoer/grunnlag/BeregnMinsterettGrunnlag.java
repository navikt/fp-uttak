package no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag;

import java.time.LocalDate;

public class BeregnMinsterettGrunnlag {

    private boolean minsterett;
    private boolean morHarUføretrygd;
    private boolean mor;
    private boolean bareFarHarRett;
    private boolean aleneomsorg;
    private Dekningsgrad dekningsgrad;
    private LocalDate familieHendelseDato;
    private LocalDate familieHendelseDatoNesteSak;

    private BeregnMinsterettGrunnlag() {
    }

    public boolean isMinsterett() {
        return minsterett;
    }

    public boolean isMorHarUføretrygd() {
        return morHarUføretrygd;
    }

    public boolean isMor() {
        return mor;
    }

    public boolean isBareFarHarRett() {
        return bareFarHarRett;
    }

    public boolean isAleneomsorg() {
        return aleneomsorg;
    }

    public Dekningsgrad getDekningsgrad() {
        return dekningsgrad;
    }

    public LocalDate getFamilieHendelseDato() {
        return familieHendelseDato;
    }

    public LocalDate getFamilieHendelseDatoNesteSak() {
        return familieHendelseDatoNesteSak;
    }

    public static class Builder {

        private BeregnMinsterettGrunnlag grunnlag = new BeregnMinsterettGrunnlag();

        public Builder minsterett(boolean minsterett) {
            grunnlag.minsterett =  minsterett;
            return this;
        }

        public Builder morHarUføretrygd(boolean morHarUføretrygd) {
            grunnlag.morHarUføretrygd =  morHarUføretrygd;
            return this;
        }

        public Builder mor(boolean mor) {
            grunnlag.mor =  mor;
            return this;
        }

        public Builder bareFarHarRett(boolean bareFarHarRett) {
            grunnlag.bareFarHarRett =  bareFarHarRett;
            return this;
        }

        public Builder aleneomsorg(boolean aleneomsorg) {
            grunnlag.aleneomsorg =  aleneomsorg;
            return this;
        }

        public Builder dekningsgrad(Dekningsgrad dekningsgrad) {
            grunnlag.dekningsgrad =  dekningsgrad;
            return this;
        }

        public Builder familieHendelseDato(LocalDate familieHendelseDato) {
            grunnlag.familieHendelseDato =  familieHendelseDato;
            return this;
        }

        public Builder familieHendelseDatoNesteSak(LocalDate familieHendelseDatoNesteSak) {
            grunnlag.familieHendelseDatoNesteSak =  familieHendelseDatoNesteSak;
            return this;
        }

        public BeregnMinsterettGrunnlag build() {
            var o = grunnlag;
            grunnlag = null;
            return o;
        }
    }



}
