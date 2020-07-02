package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.betingelser;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

import java.time.LocalDate;

@RuleDocumentation(SjekkOmPeriodenStarterFørLovligUttakFørFamiliehendelse.ID)
public class SjekkOmPeriodenStarterFørLovligUttakFørFamiliehendelse extends LeafSpecification<FastsettePeriodeGrunnlag> {

    public static final String ID = "FP_VK 27.2";
    public static final String BESKRIVELSE = "Starter uttaket tidligere enn 12 uker før familiehendelse";
    private Konfigurasjon konfigurasjon;


    public SjekkOmPeriodenStarterFørLovligUttakFørFamiliehendelse(Konfigurasjon konfigurasjon) {
        super(ID);
        this.konfigurasjon = konfigurasjon;
    }

    @Override
    public Evaluation evaluate(FastsettePeriodeGrunnlag grunnlag) {
        var hendelseDato = hendelseDato(grunnlag);
        var aktuellPeriode = grunnlag.getAktuellPeriode();
        var startDatoUttak = aktuellPeriode.getFom();
        int ukerFørFamiliehendelseUttaksgrense = konfigurasjon.getParameter(Parametertype.LOVLIG_UTTAK_FØR_FØDSEL_UKER, hendelseDato);
        if (startDatoUttak.isBefore(hendelseDato.minusWeeks(ukerFørFamiliehendelseUttaksgrense))) {
            return ja();
        }
        return nei();
    }

    private LocalDate hendelseDato(FastsettePeriodeGrunnlag grunnlag) {
        var søknadType = grunnlag.getSøknadstype();
        if (søknadType == Søknadstype.ADOPSJON) {
            return grunnlag.getFamiliehendelse();
        } else if (søknadType == Søknadstype.TERMIN) {
            // søknadsfrist regnes fra termindato ved terminsøknad
            return grunnlag.getTermindato();
        }
        return grunnlag.getFødselsdato();
    }
}
