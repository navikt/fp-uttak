package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.felles.PerioderUtenHelgUtil;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmParteneMerEnn100ProsentUttak.ID)
public class SjekkOmParteneMerEnn100ProsentUttak extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 30.0.9";
    public static final String BESKRIVELSE = "Har partene søkt mer enn 100% samlet uttak?";

    public SjekkOmParteneMerEnn100ProsentUttak() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var aktuellPeriode = grunnlag.getAktuellPeriode();
        var uttaksprosentSøker = uttaksprosent(aktuellPeriode);
        var uttaksprosentAnnenpart = uttaksprosentAnnenpart(grunnlag);

        return uttaksprosentSøker.add(uttaksprosentAnnenpart).merEnn100() ? ja() : nei();
    }

    private SamtidigUttaksprosent uttaksprosent(OppgittPeriode aktuellPeriode) {
        if (aktuellPeriode.erSøktGradering()) {
            return SamtidigUttaksprosent.HUNDRED.subtract(aktuellPeriode.getArbeidsprosent());
        }
        return aktuellPeriode.erSøktSamtidigUttak() ? aktuellPeriode.getSamtidigUttaksprosent() : SamtidigUttaksprosent.HUNDRED;
    }

    private SamtidigUttaksprosent uttaksprosentAnnenpart(FastsettePeriodeGrunnlag grunnlag) {
        var annenpartsPeriode = finnOverlappendePeriode(grunnlag.getAktuellPeriode(),
                grunnlag.getAnnenPartUttaksperioder());

        return annenpartsPeriode.map(ap -> {
            if (ap.isUtsettelse() && ap.isInnvilget()) {
                return SamtidigUttaksprosent.HUNDRED;
            }
            return ap.getAktiviteter().stream()
                    .filter(a -> a.getUtbetalingsgrad().harUtbetaling())
                    .min(Comparator.comparing(AnnenpartUttakPeriodeAktivitet::getUtbetalingsgrad))
                    .map(a -> new SamtidigUttaksprosent(a.getUtbetalingsgrad().decimalValue()))
                    .orElse(SamtidigUttaksprosent.ZERO);
        }).orElse(SamtidigUttaksprosent.ZERO);
    }

    private Optional<AnnenpartUttakPeriode> finnOverlappendePeriode(OppgittPeriode aktuellPeriode,
                                                                    List<AnnenpartUttakPeriode> annenPartUttaksperioder) {
        for (AnnenpartUttakPeriode annenpartUttakPeriode : annenPartUttaksperioder) {
            if (PerioderUtenHelgUtil.perioderUtenHelgOverlapper(aktuellPeriode, annenpartUttakPeriode)) {
                return Optional.of(annenpartUttakPeriode);
            }
        }
        return Optional.empty();
    }
}
