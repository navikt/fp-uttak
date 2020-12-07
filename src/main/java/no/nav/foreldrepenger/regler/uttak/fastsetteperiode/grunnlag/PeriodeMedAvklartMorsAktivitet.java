package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedAvklartMorsAktivitet.Resultat.IKKE_I_AKTIVITET_IKKE_DOKUMENTERT;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedAvklartMorsAktivitet.Resultat.I_AKTIVITET;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;

public class PeriodeMedAvklartMorsAktivitet extends LukketPeriode {

    private final Resultat resultat;

    public PeriodeMedAvklartMorsAktivitet(LocalDate fom, LocalDate tom, Resultat resultat) {
        super(fom, tom);
        this.resultat = resultat;
    }

    public boolean erDokumentert() {
        return !resultat.equals(IKKE_I_AKTIVITET_IKKE_DOKUMENTERT);
    }

    public boolean erIAktivitet() {
        return resultat.equals(I_AKTIVITET);
    }

    public enum Resultat {
        I_AKTIVITET,
        IKKE_I_AKTIVITET_IKKE_DOKUMENTERT,
        IKKE_I_AKTIVITET_DOKUMENTERT
    }
}
