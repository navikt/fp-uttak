package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.*;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ytelser.PleiepengerPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregning;
import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

/**
 * Interface for fastsette periode grunnlaget. Det er kun dette interfacet som skal brukes i selve regelen.
 */
@RuleDocumentationGrunnlag
public interface FastsettePeriodeGrunnlag {

    /**
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
     * Finn alle perioder der det er avklart mors aktivitet
     * Mor kan enten være i aktivitet i perioden eller ikke
     *
     * @return list av perioder.
     */
    List<PeriodeMedAvklartMorsAktivitet> getPerioderMedAvklartMorsAktivitet();

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

    List<PeriodeMedTiltakIRegiAvNav> getPerioderMedTiltakIRegiAvNav();

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
     * Tilfelle av BareFarRett og MorMottarUføretrygd, 14-14 tredje ledd
     *
     * @return true dersom rett.
     */
    boolean isBareFarHarRettMorUføretrygd();

    /**
     * Har saken perioder uten aktivitetskrav iht 14-14 tredje ledd
     * Slike saker skal ikke ha minsterett.
     *
     * @return true dersom saken tilsier dager uten aktivitetskrav.
     */
    boolean isSakMedDagerUtenAktivitetskrav();

    /**
     * Har saken en minsterett for uttak.
     * Slike saker skal ikke ha dager uten aktivitetskrav.
     *
     * @return true dersom saken tilsier en minsterett for uttak.
     */
    boolean isSakMedMinsterett();

    SaldoUtregning getSaldoUtregning();

    /**
     * Finnes alle uttaks perioder av annenpart
     *
     * @return list av annenpart sin uttaksperioder
     */
    List<AnnenpartUttakPeriode> getAnnenPartUttaksperioder();

    LocalDateTime getAnnenPartSisteSøknadMottattTidspunkt();

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
     * Eventuell dato for start av ny stønadsperiode (nytt barn)
     */
    Optional<LocalDate> getStartNyStønadsperiode();

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

    List<PeriodeMedHV> getPerioderHV();

    boolean isBerørtBehandling();

    LocalDateTime getSisteSøknadMottattTidspunkt();

    boolean kreverBehandlingSammenhengendeUttak();

    Collection<PleiepengerPeriode> perioderMedPleiepenger();
}
