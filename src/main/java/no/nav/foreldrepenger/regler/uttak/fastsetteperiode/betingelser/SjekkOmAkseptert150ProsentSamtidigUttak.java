package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FELLESPERIODE;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.SamtidigUttaksprosent;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmAkseptert150ProsentSamtidigUttak.ID)
public class SjekkOmAkseptert150ProsentSamtidigUttak extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 30.0.11";
    public static final String BESKRIVELSE = "Er det akseptert inntil 150% samlet uttak?";

    private static final Set<Stønadskontotype> AKT_KONTI = Set.of(Stønadskontotype.MØDREKVOTE, Stønadskontotype.FEDREKVOTE, FELLESPERIODE);
    private static final Set<Stønadskontotype> KVOTE_KONTI = Set.of(Stønadskontotype.MØDREKVOTE, Stønadskontotype.FEDREKVOTE);

    public SjekkOmAkseptert150ProsentSamtidigUttak() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var aktuellPeriode = grunnlag.getAktuellPeriode();
        var annenpartsOverlappKonto = grunnlag.getAnnenPartUttaksperiodeSomOverlapperAktuellPeriode(app -> true)
            .map(AnnenpartUttakPeriode::getAktiviteter).orElse(Set.of()).stream()
            .map(AnnenpartUttakPeriodeAktivitet::getStønadskontotype)
            .filter(AKT_KONTI::contains)
            .collect(Collectors.toSet());
        // Ser etter kombo MK/FK (<= 100%) + Fellesperiode (<= 50%)
        if (annenpartsOverlappKonto.isEmpty() || !AKT_KONTI.contains(aktuellPeriode.getStønadskontotype()) ||
            (KVOTE_KONTI.contains(aktuellPeriode.getStønadskontotype()) && !annenpartsOverlappKonto.contains(Stønadskontotype.FELLESPERIODE)) ||
            (FELLESPERIODE.equals(aktuellPeriode.getStønadskontotype()) && annenpartsOverlappKonto.stream().noneMatch(KVOTE_KONTI::contains))) {
            return nei();
        }
        if (FELLESPERIODE.equals(aktuellPeriode.getStønadskontotype())) {
            return uttaksprosent(aktuellPeriode).merEnn50() ? nei() : ja();
        } else {
            return uttaksprosentAnnenpart(grunnlag).merEnn50() ? nei() : ja();
        }
    }

    private SamtidigUttaksprosent uttaksprosent(OppgittPeriode aktuellPeriode) {
        if (aktuellPeriode.erSøktGradering()) {
            return SamtidigUttaksprosent.HUNDRED.subtract(aktuellPeriode.getArbeidsprosent());
        }
        return aktuellPeriode.erSøktSamtidigUttak() ? aktuellPeriode.getSamtidigUttaksprosent() : SamtidigUttaksprosent.HUNDRED;
    }

    private SamtidigUttaksprosent uttaksprosentAnnenpart(FastsettePeriodeGrunnlag grunnlag) {
        var annenpartsPeriode = grunnlag.getAnnenPartUttaksperiodeSomOverlapperAktuellPeriode(app -> true);

        return annenpartsPeriode.map(this::getSamtidigUttaksprosent).orElse(SamtidigUttaksprosent.ZERO);
    }

    private SamtidigUttaksprosent getSamtidigUttaksprosent(AnnenpartUttakPeriode ap) {
        if (ap.isUtsettelse() && ap.isInnvilget()) {
            return SamtidigUttaksprosent.HUNDRED;
        }
        return ap.getAktiviteter().stream()
            .filter(a -> a.getUtbetalingsgrad().harUtbetaling())
            .min(Comparator.comparing(AnnenpartUttakPeriodeAktivitet::getUtbetalingsgrad))
            .map(a -> new SamtidigUttaksprosent(a.getUtbetalingsgrad().decimalValue()))
            .orElse(SamtidigUttaksprosent.ZERO);
    }
}
