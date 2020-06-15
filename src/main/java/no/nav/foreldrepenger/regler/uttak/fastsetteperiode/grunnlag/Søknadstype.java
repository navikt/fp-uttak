package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

public enum Søknadstype {
    FØDSEL, TERMIN, ADOPSJON;

    public boolean gjelderTerminFødsel() {
        return this == TERMIN || this == FØDSEL;
    }
}
