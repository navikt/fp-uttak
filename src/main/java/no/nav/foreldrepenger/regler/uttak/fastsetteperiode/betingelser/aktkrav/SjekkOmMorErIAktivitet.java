package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser.aktkrav;


import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.DokumentasjonVurdering.MORS_AKTIVITET_GODKJENT;

import java.math.BigDecimal;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetskravArbeidPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetskravGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.MorsAktivitet;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmMorErIAktivitet.ID)
public class SjekkOmMorErIAktivitet extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "AVSLAG_AKT_9";
    public static final String BESKRIVELSE = "Er det avklart at mor er i aktivitet?";

    public SjekkOmMorErIAktivitet() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        return erIAktivitet(grunnlag, 75) ? ja() : nei();
    }

    static boolean erIAktivitet(FastsettePeriodeGrunnlag grunnlag, int påkrevdStilling) {
        var aktuellPeriode = grunnlag.getAktuellPeriode();
        var dokumentasjonVurdering = aktuellPeriode.getDokumentasjonVurdering();
        if (dokumentasjonVurdering == null) {
            //Ser ikke på permisjonsprosent, ettersom alle perioder med permisjon er avklart av saksbehandler med tilsvarende dokumentasjonvurdering
            return MorsAktivitet.ARBEID.equals(aktuellPeriode.getMorsAktivitet()) && grunnlag.getAktivitetskravGrunnlag()
                .map(ag -> merEllerLikStilling(ag, aktuellPeriode, påkrevdStilling))
                .orElse(false);
        }
        return MORS_AKTIVITET_GODKJENT.equals(dokumentasjonVurdering);
    }

    private static boolean merEllerLikStilling(AktivitetskravGrunnlag ag, LukketPeriode periode, int påkrevdStilling) {
        return ag.perioder().stream().filter(ap -> ap.overlapper(periode))
            .map(AktivitetskravArbeidPeriode::getStillingsprosent)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .compareTo(BigDecimal.valueOf(påkrevdStilling)) >= 0;
    }
}
