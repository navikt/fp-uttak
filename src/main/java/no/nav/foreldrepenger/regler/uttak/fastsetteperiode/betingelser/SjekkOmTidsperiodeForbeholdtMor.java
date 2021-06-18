package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmTidsperiodeForbeholdtMor.ID)
public class SjekkOmTidsperiodeForbeholdtMor extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "TODO fritt uttak";
    public static final String BESKRIVELSE = "Er perioden i tidsperioden forbeholdt mor?";

    private final Konfigurasjon konfigurasjon;

    public SjekkOmTidsperiodeForbeholdtMor(Konfigurasjon konfigurasjon) {
        super(ID);
        this.konfigurasjon = konfigurasjon;
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var familiehendelse = grunnlag.getFamiliehendelse();
        var periode = grunnlag.getAktuellPeriode();

        var fomDatoForbeholdtMor = fomDatoForbeholdtMor(familiehendelse);
        var tomDatoForbeholdtMor = tomDatoForbeholdtMor(familiehendelse);

        //Regner med at oppgitte perioder er knekk riktig
        return periode.overlapper(new LukketPeriode(fomDatoForbeholdtMor, tomDatoForbeholdtMor)) ? ja() : nei();
    }

    private LocalDate fomDatoForbeholdtMor(LocalDate familiehendelse) {
        var antallUkerFør = konfigurasjon.getParameter(Parametertype.UTTAK_FELLESPERIODE_FØR_FØDSEL_UKER, familiehendelse);
        return familiehendelse.minusWeeks(antallUkerFør).minusDays(1);
    }

    private LocalDate tomDatoForbeholdtMor(LocalDate familiehendelse) {
        var antallUkerEtter = konfigurasjon.getParameter(Parametertype.UTTAK_MØDREKVOTE_ETTER_FØDSEL_UKER, familiehendelse);
        return familiehendelse.plusWeeks(antallUkerEtter).minusDays(1);
    }
}
