package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Adopsjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GyldigGrunnPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedBarnInnlagt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedInnleggelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedSykdomEllerSkade;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeUtenOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregning;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

/**
 * Interface for fastsette periode grunnlaget. Det er kun dette interfacet som skal brukes i selve regelen.
 */
@RuleDocumentationGrunnlag
public interface FastsettePeriodeGrunnlag {

    /**
     *
     * @return Aktuell periode. Det er den perioden som skal behandles av regel.
     */
    OppgittPeriode getAktuellPeriode();

    /**
     * Hent arbeidsgrunnlag
     *
     * @return arbeidsgrunnlag for alle arbeidsforhold/aktiviteter
     */
    Arbeid getArbeid();

    /**
     * Finn perioder der søker har gyldig grunn for tidlig oppstart eller utsettelse.
     *
     * @return Array av aktuelle perioder med gyldig grunn, sortert på fom dato. Returnerer tom list om det ikke finnes en aktuell periode eller om det ikke finnes overlappende
     * perioder med gyldig grunn.
     */
    List<GyldigGrunnPeriode> getAktuelleGyldigeGrunnPerioder();

    /**
     * Finn søknadstype.
     *
     * @return søknadstype.
     */
    Søknadstype getSøknadstype();

    /**
     * Finner dato for familiehendelsen som søknaden gjelder. Kan være dato for termin, fødsel eller omsorgsovertakelse.
     *
     * @return dato for familiehendelse.
     */
    LocalDate getFamiliehendelse();

    /**
     * Finn ut om søker er mor til barn som det er søkt stønad for.
     *
     * @return true dersom søker er mor, ellers false.
     */
    boolean isSøkerMor();

    /**
     * Finn ut om annen forelder er kjent med hvilke perioder det er søkt om.
     *
     * @return true dersom det er informert, ellers false.
     */
    boolean isSamtykke();

    /**
     * Finn første dato for når gyldig uttak kan starte basert på søknadsfrist.
     *
     * @return første dato for når gyldig uttak kan starte.
     */
    LocalDate getFørsteLovligeUttaksdag();

    /**
     * Finn alle perioder med gyldig grunn for tidlig oppstart
     *
     * @return list av perioder med gyldig grunn for tidlig oppstart
     */
    List<GyldigGrunnPeriode> getGyldigGrunnPerioder();

    /**
     * Finn alle perioder der søker ikke har omsorg for barnet/barna det søkes om
     *
     * @return list av perioder der søker ikke har omsorg for barnet/barna det søkes om
     */
    List<PeriodeUtenOmsorg> getPerioderUtenOmsorg();

    /**
     * Finn alle perioder der søker har bekreftet sykdom eller skade.
     *
     * @return list av perioder der søker har bekreftet sykdom eller skade.
     */
    List<PeriodeMedSykdomEllerSkade> getPerioderMedSykdomEllerSkade();

    /**
     * Finn alle perioder der søker har bekreftet innleggelse.
     *
     * @return list av perioder der søker har bekreftet innleggelse.
     */
    List<PeriodeMedInnleggelse> getPerioderMedInnleggelse();

    /**
     * Finn alle perioder der søkers barn er innlagt på helseinstitusjon.
     *
     * @return list av perioder der søkers barn er innlagt på helseinstitusjon.
     */
    List<PeriodeMedBarnInnlagt> getPerioderMedBarnInnlagt();

    /**
     * Finn alle perioder der søkers annen forelder er innlagt på helseinstitusjon.
     *
     * @return list av perioder der søkers annen forelder er innlagt på helseinstitusjon.
     */
    List<OppgittPeriode> getPerioderMedAnnenForelderInnlagt();

    /**
     * Finn alle perioder der søkers annen forelder har bekreftet sykdom eller skade.
     *
     * @return list av perioder der søkers annen forelder har bekreftet sykdom eller skade.
     */
    List<OppgittPeriode> getPerioderMedAnnenForelderSykdomEllerSkade();

    /**
     * Finn alle perioder der søkers annen forelder har bekreftet ikke rett.
     *
     * @return list av perioder der søkers annen forelder har bekreftet ikke rett.
     */
    List<OppgittPeriode> getPerioderMedAnnenForelderIkkeRett();

    /**
     * Finn alle perioder der søkers annen forelder har bekreftet ikke omsorg.
     *
     * @return list av perioder der søkers annen forelder har bekreftet ikke omsorg.
     */
    List<OppgittPeriode> getPerioderMedAleneomsorg();

    /**
     * Har far/medmor rett til foreldrepenger.
     *
     * @return true dersom rett.
     */
    boolean isFarRett();

    /**
     * Har mor rett til foreldrepenger.
     *
     * @return true dersom rett.
     */

    boolean isMorRett();

    /**
     * Dato for mottatt siste søknad
     * @return dato
     */
    LocalDate getSøknadMottattdato();

    SaldoUtregning getSaldoUtregning();

    /**
     * Finnes alle uttaks perioder av annenpart
     * @return list av annenpart sin uttaksperioder
     */
    List<AnnenpartUttakPeriode> getAnnenPartUttaksperioder();

    boolean harAleneomsorg();

    /**
     * Om det finnes en opphørsdato der søker ikke lengre oppfyller medlemskapsvilkåret
     */
    LocalDate getOpphørsdatoForMedlemskap();

    /**
     * Om det finnes en dødsdato for søker
     */
    LocalDate getDødsdatoForSøker();

    /**
     * Om det finnes en dødsdato for barn
     */
    LocalDate getDødsdatoForBarn();

    /**
     * Om alle barn er døde eller ikke
     */
    boolean erAlleBarnDøde();

    /**
     * Inngangsvilkår
     */
    Inngangsvilkår getInngangsvilkår();

    /**
     * Adopsjon
     */
    Adopsjon getAdopsjon();

    /**
     * Søkers stønadskontotyper
     */
    Set<Stønadskontotype> getGyldigeStønadskontotyper();

    /**
     * Fødselsdato
     */
    LocalDate getFødselsdato();

    /**
     * Termindato
     */
    LocalDate getTermindato();

    boolean isTapendeBehandling();
}
