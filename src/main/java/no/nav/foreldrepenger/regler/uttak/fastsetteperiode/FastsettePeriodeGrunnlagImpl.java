package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Adopsjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GyldigGrunnPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedAvklartMorsAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedBarnInnlagt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedHV;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedInnleggelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedSykdomEllerSkade;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedTiltakIRegiAvNav;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeUtenOmsorg;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Spesialkontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ytelser.PleiepengerPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregning;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Periode;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;

public class FastsettePeriodeGrunnlagImpl implements FastsettePeriodeGrunnlag {

    private final RegelGrunnlag regelGrunnlag;

    private final SaldoUtregning saldoUtregning;

    private final OppgittPeriode aktuellPeriode;

    public FastsettePeriodeGrunnlagImpl(RegelGrunnlag regelGrunnlag, SaldoUtregning saldoUtregning, OppgittPeriode aktuellPeriode) {
        this.regelGrunnlag = regelGrunnlag;
        this.saldoUtregning = saldoUtregning;
        this.aktuellPeriode = aktuellPeriode;
    }

    @Override
    public OppgittPeriode getAktuellPeriode() {
        return aktuellPeriode;
    }

    @Override
    public List<GyldigGrunnPeriode> getAktuelleGyldigeGrunnPerioder() {
        var dokumentasjon = regelGrunnlag.getSøknad().getDokumentasjon();
        if (dokumentasjon == null) {
            return Collections.emptyList();
        }
        return dokumentasjon.getGyldigGrunnPerioder()
                .stream()
                .filter(p -> p.overlapper(aktuellPeriode))
                .sorted(Comparator.comparing(Periode::getFom))
                .collect(Collectors.toList());
    }

    @Override
    public Arbeid getArbeid() {
        return regelGrunnlag.getArbeid();
    }

    @Override
    public List<PeriodeMedSykdomEllerSkade> getPerioderMedSykdomEllerSkade() {
        return regelGrunnlag.getSøknad().getDokumentasjon().getPerioderMedSykdomEllerSkade();
    }

    @Override
    public List<PeriodeMedInnleggelse> getPerioderMedInnleggelse() {
        return regelGrunnlag.getSøknad().getDokumentasjon().getPerioderMedInnleggelse();
    }

    @Override
    public List<PeriodeMedAvklartMorsAktivitet> getPerioderMedAvklartMorsAktivitet() {
        return regelGrunnlag.getSøknad().getDokumentasjon().getPerioderMedAvklartMorsAktivitet();
    }

    @Override
    public List<PeriodeMedBarnInnlagt> getPerioderMedBarnInnlagt() {
        var fraDok = regelGrunnlag.getSøknad().getDokumentasjon().getPerioderMedBarnInnlagt();
        var felles = new ArrayList<>(fraDok);
        var fraYtelser = regelGrunnlag.getYtelser().pleiepenger()
                .map(pleiepengerMedInnleggelse -> pleiepengerMedInnleggelse.innleggelser())
                .orElse(List.of())
                .stream()
                .map(p -> new PeriodeMedBarnInnlagt(p.getFom(), p.getTom()))
                .toList();
        felles.addAll(fraYtelser);
        return felles;
    }

    @Override
    public List<OppgittPeriode> getPerioderMedAnnenForelderInnlagt() {
        return getPerioderMedOverføringÅrsak(OverføringÅrsak.INNLEGGELSE);
    }

    @Override
    public List<OppgittPeriode> getPerioderMedAnnenForelderSykdomEllerSkade() {
        return getPerioderMedOverføringÅrsak(OverføringÅrsak.SYKDOM_ELLER_SKADE);
    }

    @Override
    public List<PeriodeMedHV> getPerioderHV() {
        return regelGrunnlag.getSøknad().getDokumentasjon().getPerioderMedHv();
    }

    @Override
    public List<PeriodeMedTiltakIRegiAvNav> getPerioderMedTiltakIRegiAvNav() {
        return regelGrunnlag.getSøknad().getDokumentasjon().getPerioderMedTiltakViaNav();
    }

    @Override
    public List<OppgittPeriode> getPerioderMedAnnenForelderIkkeRett() {
        return getPerioderMedOverføringÅrsak(OverføringÅrsak.ANNEN_FORELDER_IKKE_RETT);
    }

    @Override
    public List<OppgittPeriode> getPerioderMedAleneomsorg() {
        return getPerioderMedOverføringÅrsak(OverføringÅrsak.ALENEOMSORG);
    }

    private List<OppgittPeriode> getPerioderMedOverføringÅrsak(OverføringÅrsak årsak) {
        return regelGrunnlag.getSøknad()
                .getOppgittePerioder()
                .stream()
                .filter(periode -> Objects.equals(periode.getOverføringÅrsak(), årsak) && PeriodeVurderingType.avklart(
                        periode.getPeriodeVurderingType()))
                .collect(Collectors.toList());
    }

    @Override
    public Søknadstype getSøknadstype() {
        return regelGrunnlag.getSøknad().getType();
    }

    @Override
    public LocalDate getFamiliehendelse() {
        return regelGrunnlag.getDatoer().getFamiliehendelse();
    }

    @Override
    public boolean isSøkerMor() {
        return regelGrunnlag.getBehandling().isSøkerMor();
    }

    @Override
    public boolean isSamtykke() {
        return regelGrunnlag.getRettOgOmsorg().getSamtykke();
    }

    @Override
    public boolean isFarRett() {
        return regelGrunnlag.getRettOgOmsorg().getFarHarRett();
    }

    @Override
    public boolean isMorRett() {
        return regelGrunnlag.getRettOgOmsorg().getMorHarRett();
    }

    @Override
    public boolean isMorOppgittUføretrygd() {
        return regelGrunnlag.getRettOgOmsorg().getMorOppgittUføretrygd();
    }

    @Override
    public boolean isBareFarHarRettMorUføretrygd() {
        var rett = regelGrunnlag.getRettOgOmsorg();
        return rett.getFarHarRett() && !rett.getMorHarRett() && rett.getMorUføretrygd();
    }

    @Override
    public boolean isSakMedMinsterett() {
        return regelGrunnlag.getKontoer().harSpesialkonto(Spesialkontotype.MINSTERETT) && regelGrunnlag.getKontoer().getSpesialkontoTrekkdager(Spesialkontotype.MINSTERETT) > 0;
    }

    @Override
    public boolean isSakMedDagerUtenAktivitetskrav() {
        return regelGrunnlag.getKontoer().harSpesialkonto(Spesialkontotype.UTEN_AKTIVITETSKRAV) && regelGrunnlag.getKontoer().getSpesialkontoTrekkdager(Spesialkontotype.UTEN_AKTIVITETSKRAV) > 0;
    }

    @Override
    public List<GyldigGrunnPeriode> getGyldigGrunnPerioder() {
        return regelGrunnlag.getSøknad().getDokumentasjon().getGyldigGrunnPerioder();
    }

    @Override
    public List<PeriodeUtenOmsorg> getPerioderUtenOmsorg() {
        var dokumentasjon = regelGrunnlag.getSøknad().getDokumentasjon();
        if (dokumentasjon == null) {
            return Collections.emptyList();
        }
        return dokumentasjon.getPerioderUtenOmsorg();
    }

    @Override
    public SaldoUtregning getSaldoUtregning() {
        return saldoUtregning;
    }

    @Override
    public List<AnnenpartUttakPeriode> getAnnenPartUttaksperioder() {
        return regelGrunnlag.getAnnenPart() != null ? regelGrunnlag.getAnnenPart().getUttaksperioder() : Collections.emptyList();
    }

    @Override
    public LocalDateTime getAnnenPartSisteSøknadMottattTidspunkt() {
        return regelGrunnlag.getAnnenPart().getSisteSøknadMottattTidspunkt();
    }

    @Override
    public boolean harAleneomsorg() {
        return regelGrunnlag.getRettOgOmsorg().getAleneomsorg();
    }

    @Override
    public LocalDate getOpphørsdatoForMedlemskap() {
        return regelGrunnlag.getMedlemskap() == null ? null : regelGrunnlag.getMedlemskap().getOpphørsdato();
    }

    @Override
    public LocalDate getDødsdatoForSøker() {
        return regelGrunnlag.getDatoer().getDødsdatoer() == null ? null : regelGrunnlag.getDatoer()
                .getDødsdatoer()
                .getSøkersDødsdato();
    }

    @Override
    public LocalDate getDødsdatoForBarn() {
        return regelGrunnlag.getDatoer().getDødsdatoer() == null ? null : regelGrunnlag.getDatoer().getDødsdatoer().getBarnsDødsdato();
    }

    @Override
    public boolean erAlleBarnDøde() {
        return regelGrunnlag.getDatoer().getDødsdatoer() != null && regelGrunnlag.getDatoer().getDødsdatoer().erAlleBarnDøde();
    }

    @Override
    public Optional<LocalDate> getStartNyStønadsperiode() {
        return regelGrunnlag.getDatoer().getStartdatoNesteStønadsperiode();
    }

    @Override
    public Inngangsvilkår getInngangsvilkår() {
        return regelGrunnlag.getInngangsvilkår();
    }

    @Override
    public Adopsjon getAdopsjon() {
        return regelGrunnlag.getAdopsjon();
    }

    @Override
    public Set<Stønadskontotype> getGyldigeStønadskontotyper() {
        return regelGrunnlag.getGyldigeStønadskontotyper();
    }

    @Override
    public LocalDate getFødselsdato() {
        return regelGrunnlag.getDatoer().getFødsel();
    }

    @Override
    public LocalDate getTermindato() {
        return regelGrunnlag.getDatoer().getTermin();
    }

    @Override
    public boolean isBerørtBehandling() {
        return regelGrunnlag.getBehandling().isBerørtBehandling();
    }

    @Override
    public LocalDateTime getSisteSøknadMottattTidspunkt() {
        return regelGrunnlag.getSøknad().getMottattTidspunkt();
    }

    @Override
    public boolean kreverBehandlingSammenhengendeUttak() {
        return regelGrunnlag.getBehandling().isKreverSammenhengendeUttak();
    }

    @Override
    public Optional<LukketPeriode> periodeFarRundtFødsel(Konfigurasjon konfigurasjon) {
        return FarUttakRundtFødsel.utledFarsPeriodeRundtFødsel(regelGrunnlag, konfigurasjon);
    }

    @Override
    public Collection<PleiepengerPeriode> perioderMedPleiepenger() {
        return regelGrunnlag.getYtelser().pleiepenger().map(p -> p.perioder()).orElse(List.of());
    }
}
