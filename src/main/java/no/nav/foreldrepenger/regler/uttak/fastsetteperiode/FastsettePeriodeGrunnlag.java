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
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
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
     * Om bruker har omsorg for barnet
     *
     * @return true dersom bruker har omsorg for barnet
     */
    boolean harOmsorg();

    List<PleiepengerPeriode> getPleiepengerInnleggelse();

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
     * Tilfelle av BareFarRett og oppgitt at MorMottarUføretrygd, 14-14 tredje ledd
     *
     * @return true dersom oppgitt.
     */
    boolean isMorOppgittUføretrygd();

    /**
     * Tilfelle av BareFarRett og MorMottarUføretrygd, 14-14 tredje ledd
     *
     * @return true dersom bare far rett og bekreftet uføretrygd.
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

    /**
     * Har saken en minsterett for uttak som gjelder etter start av neste stønadsperiode.
     *
     * @return true dersom saken tilsier en minsterett for uttak.
     */
    boolean isSakMedRettEtterStartNesteStønadsperiode();

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
     * Aktuell periode begyunner på eller etter startdato neste stønadsperiode
     */
    boolean erAktuellPeriodeEtterStartNesteStønadsperiode();

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

    boolean isBerørtBehandling();

    LocalDateTime getSisteSøknadMottattTidspunkt();

    boolean kreverBehandlingSammenhengendeUttak();

    Optional<LukketPeriode> periodeFarRundtFødsel();

    Collection<PleiepengerPeriode> perioderMedPleiepenger();
}
