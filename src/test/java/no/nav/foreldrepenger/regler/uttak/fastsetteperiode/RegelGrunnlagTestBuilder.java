package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Konto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Kontoer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Opptjening;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RettOgOmsorg;


public class RegelGrunnlagTestBuilder {

    public static final AktivitetIdentifikator ARBEIDSFORHOLD_1 = AktivitetIdentifikator.forArbeid("000000001", null);
    public static final AktivitetIdentifikator ARBEIDSFORHOLD_2 = AktivitetIdentifikator.forArbeid("000000002", null);
    public static final AktivitetIdentifikator ARBEIDSFORHOLD_3 = AktivitetIdentifikator.forArbeid("000000003", null);

    public static RegelGrunnlag.Builder create() {
        var kontoer = new Kontoer.Builder()
                .leggTilKonto(new Konto.Builder().medType(FORELDREPENGER_FØR_FØDSEL).medTrekkdager(15))
                .leggTilKonto(new Konto.Builder().medType(MØDREKVOTE).medTrekkdager(75))
                .leggTilKonto(new Konto.Builder().medType(FEDREKVOTE).medTrekkdager(75))
                .leggTilKonto(new Konto.Builder().medType(FELLESPERIODE).medTrekkdager(80));
        var arbeidsforhold = new Arbeidsforhold(ARBEIDSFORHOLD_1, kontoer);
        return new RegelGrunnlag.Builder()
                .medOpptjening(new Opptjening.Builder().medSkjæringstidspunkt(LocalDate.MIN))
                .medBehandling(new Behandling.Builder().medSøkerErMor(true))
                .medRettOgOmsorg(new RettOgOmsorg.Builder().medMorHarRett(true).medFarHarRett(true).medSamtykke(true))
                .medArbeid(new Arbeid.Builder().leggTilArbeidsforhold(arbeidsforhold))
                .medInngangsvilkår(new Inngangsvilkår.Builder()
                        .medAdopsjonOppfylt(true)
                        .medForeldreansvarnOppfylt(true)
                        .medFødselOppfylt(true)
                        .medOpptjeningOppfylt(true));
    }

}
