package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EndringAvStilling(LocalDate dato, BigDecimal summertStillingsprosent) {}
