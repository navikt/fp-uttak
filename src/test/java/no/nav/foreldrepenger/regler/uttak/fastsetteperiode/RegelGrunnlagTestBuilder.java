package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype.MØDREKVOTE;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Opptjening;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Orgnummer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;


public class RegelGrunnlagTestBuilder {

    public static final AktivitetIdentifikator ARBEIDSFORHOLD_1 = AktivitetIdentifikator.forArbeid(new Orgnummer("000000001"), null);
    public static final AktivitetIdentifikator ARBEIDSFORHOLD_2 = AktivitetIdentifikator.forArbeid(new Orgnummer("000000002"), null);
    public static final AktivitetIdentifikator ARBEIDSFORHOLD_3 = AktivitetIdentifikator.forArbeid(new Orgnummer("000000003"), null);

    public static RegelGrunnlag.Builder create() {
        var kontoer = new Kontoer.Builder().konto(new Konto.Builder().type(FORELDREPENGER_FØR_FØDSEL).trekkdager(15))
                .konto(new Konto.Builder().type(MØDREKVOTE).trekkdager(75))
                .konto(new Konto.Builder().type(FEDREKVOTE).trekkdager(75))
                .konto(new Konto.Builder().type(FELLESPERIODE).trekkdager(80));
        var arbeidsforhold = new Arbeidsforhold(ARBEIDSFORHOLD_1);
        return new RegelGrunnlag.Builder().kontoer(kontoer)
                .opptjening(new Opptjening.Builder().skjæringstidspunkt(LocalDate.MIN))
                .behandling(new Behandling.Builder().søkerErMor(true))
                .rettOgOmsorg(new RettOgOmsorg.Builder().morHarRett(true).farHarRett(true).samtykke(true))
                .arbeid(new Arbeid.Builder().arbeidsforhold(arbeidsforhold))
                .inngangsvilkår(new Inngangsvilkår.Builder().adopsjonOppfylt(true)
                        .foreldreansvarnOppfylt(true)
                        .fødselOppfylt(true)
                        .opptjeningOppfylt(true));
    }

}
