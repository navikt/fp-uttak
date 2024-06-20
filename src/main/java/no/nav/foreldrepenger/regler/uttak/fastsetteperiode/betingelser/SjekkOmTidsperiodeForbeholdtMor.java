package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.time.LocalDate;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig.Parametertype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmTidsperiodeForbeholdtMor.ID)
public class SjekkOmTidsperiodeForbeholdtMor extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 10.2.1";
    public static final String BESKRIVELSE = "Er perioden i tidsperioden forbeholdt mor?";

    public SjekkOmTidsperiodeForbeholdtMor() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var familiehendelse = grunnlag.getFamiliehendelse();
        var periode = grunnlag.getAktuellPeriode();

        var fomDatoForbeholdtMor = fomDatoForbeholdtMor(familiehendelse);
        var tomDatoForbeholdtMor = tomDatoForbeholdtMor(familiehendelse);

        // Regner med at oppgitte perioder er knekk riktig
        return periode.overlapper(new LukketPeriode(fomDatoForbeholdtMor, tomDatoForbeholdtMor)) ? ja() : nei();
    }

    private LocalDate fomDatoForbeholdtMor(LocalDate familiehendelse) {
        var antallUkerFør =
                Konfigurasjon.STANDARD.getParameter(Parametertype.SENEST_UTTAK_FØR_TERMIN_UKER, familiehendelse);
        return familiehendelse.minusWeeks(antallUkerFør);
    }

    private LocalDate tomDatoForbeholdtMor(LocalDate familiehendelse) {
        var antallUkerEtter =
                Konfigurasjon.STANDARD.getParameter(Parametertype.FORBEHOLDT_MOR_ETTER_FØDSEL_UKER, familiehendelse);
        return familiehendelse.plusWeeks(antallUkerEtter).minusDays(1);
    }
}
