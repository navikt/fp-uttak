package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ytelser;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;

public class PleiepengerPeriode extends LukketPeriode {

    private final boolean barnInnlagt;

    public PleiepengerPeriode(LocalDate fom, LocalDate tom, boolean barnInnlagt) {
        super(fom, tom);
        this.barnInnlagt = barnInnlagt;
    }

    public boolean isBarnInnlagt() {
        return barnInnlagt;
    }
}
