package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class ManglendeSøktPeriode extends StønadsPeriode {

    public ManglendeSøktPeriode(Stønadskontotype stønadskontotype,
                                LocalDate fom,
                                LocalDate tom) {
        super(stønadskontotype, PeriodeKilde.SØKNAD, fom, tom, null, false);
    }

    private ManglendeSøktPeriode(ManglendeSøktPeriode kilde, LocalDate fom, LocalDate tom) {
        super(kilde, fom, tom);
    }

    @Override
    public ManglendeSøktPeriode kopiMedNyPeriode(LocalDate fom, LocalDate tom) {
        return new ManglendeSøktPeriode(this, fom, tom);
    }
}
