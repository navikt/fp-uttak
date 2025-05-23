package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Adopsjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetskravGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Rettighetstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
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
     * Tilfelle av BareFarRett og oppgitt at MorMottarUføretrygd, 14-14 tredje ledd
     *
     * @return true dersom oppgitt.
     */
    boolean isMorOppgittUføretrygd();

    /**
     * Har saken perioder uten aktivitetskrav iht 14-14 tredje ledd
     * Slike saker skal ikke ha minsterett.
     *
     * @return true dersom saken tilsier dager uten aktivitetskrav.
     */
    boolean isSakMedDagerUtenAktivitetskrav();

    Rettighetstype rettighetsType();

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

    Optional<LukketPeriode> periodeFarRundtFødsel();

    Collection<PleiepengerPeriode> perioderMedPleiepenger();

    LocalDate getSammenhengendeUttakTomDato();

    Optional<AktivitetskravGrunnlag> getAktivitetskravGrunnlag();
}
