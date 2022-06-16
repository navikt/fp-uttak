package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.Trekkdager;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;

public record Stønadskonto(Stønadskontotype stønadskontoType, Trekkdager maksdager) {

}
