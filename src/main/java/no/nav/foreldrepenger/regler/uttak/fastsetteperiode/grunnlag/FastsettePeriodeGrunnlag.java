package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

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
    UttakPeriode getAktuellPeriode();

    List<AktivitetIdentifikator> getAktiviteter();

    /**
     * Hent arbeidsprosenter for alle arbeidsforhold/aktiviteter
     *
     * @return arbeidsprosenter for alle arbeidsforhold/aktiviteter
     */
    Arbeidsprosenter getArbeidsprosenter();

    /**
     * Finn perioder der søker har gyldig grunn for tidlig oppstart eller utsettelse.
     *
     * @return Array av aktuelle perioder med gyldig grunn, sortert på fom dato. Returnerer tom list om det ikke finnes en aktuell periode eller om det ikke finnes overlappende
     * perioder med gyldig grunn.
     */
    List<GyldigGrunnPeriode> getAktuelleGyldigeGrunnPerioder();

    /**
     * Finn stønadskontotype for aktuell periode.
     *
     * @return stønadskontotype. Returmerer Stønadskontotype.UKJENT dersom det ikke er noen aktuell periode.
     */
    Stønadskontotype getStønadskontotype();

    /**
     * Finn søknadstype.
     *
     * @return søknadstype.
     */
    Søknadstype getSøknadstype();

    /**
     * Finn behandlingType.
     *
     * @return behandlingType.
     */
    Behandlingtype getBehandlingtype();

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
    List<UttakPeriode> getPerioderMedAnnenForelderInnlagt();

    /**
     * Finn alle perioder der søkers annen forelder har bekreftet sykdom eller skade.
     *
     * @return list av perioder der søkers annen forelder har bekreftet sykdom eller skade.
     */
    List<UttakPeriode> getPerioderMedAnnenForelderSykdomEllerSkade();

    /**
     * Finn alle perioder der søkers annen forelder har bekreftet ikke rett.
     *
     * @return list av perioder der søkers annen forelder har bekreftet ikke rett.
     */
    List<UttakPeriode> getPerioderMedAnnenForelderIkkeRett();

    /**
     * Finn alle perioder der søkers annen forelder har bekreftet ikke omsorg.
     *
     * @return list av perioder der søkers annen forelder har bekreftet ikke omsorg.
     */
    List<UttakPeriode> getPerioderMedAleneomsorg();

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
     * Dato for mottatt endringssøknad
     * @return dato
     */
    LocalDate getEndringssøknadMottattdato();

    /**
     * Om dette er en endringssøknad eller ikke
     */
    boolean erEndringssøknad();

    Trekkdagertilstand getTrekkdagertilstand();

    /**
     * Finnes alle uttaks perioder av annenpart
     * @return list av annenpart sin uttaksperioder
     */
    List<AnnenpartUttaksperiode> getAnnenPartUttaksperioder();

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
}
