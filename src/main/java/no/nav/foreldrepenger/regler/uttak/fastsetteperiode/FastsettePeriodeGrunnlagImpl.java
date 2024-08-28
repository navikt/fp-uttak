package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Adopsjon;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AnnenpartUttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Inngangsvilkår;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppgittPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.RegelGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Spesialkontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknad;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ytelser.PleiepengerPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.saldo.SaldoUtregning;

public class FastsettePeriodeGrunnlagImpl implements FastsettePeriodeGrunnlag {

    private final RegelGrunnlag regelGrunnlag;

    private final SaldoUtregning saldoUtregning;

    private final OppgittPeriode aktuellPeriode;

    private final LukketPeriode farRundtFødselIntervall;

    public FastsettePeriodeGrunnlagImpl(RegelGrunnlag regelGrunnlag,
                                        LukketPeriode farRundtFødselIntervall,
                                        SaldoUtregning saldoUtregning,
                                        OppgittPeriode aktuellPeriode) {
        this.regelGrunnlag = regelGrunnlag;
        this.saldoUtregning = saldoUtregning;
        this.aktuellPeriode = aktuellPeriode;
        this.farRundtFødselIntervall = farRundtFødselIntervall;
    }

    @Override
    public OppgittPeriode getAktuellPeriode() {
        return aktuellPeriode;
    }

    @Override
    public Arbeid getArbeid() {
        return regelGrunnlag.getArbeid();
    }

    @Override
    public List<PleiepengerPeriode> getPleiepengerInnleggelse() {
        return regelGrunnlag.getYtelser()
            .pleiepenger()
            .map(pleiepengerMedInnleggelse -> pleiepengerMedInnleggelse.innleggelser())
            .orElse(List.of())
            .stream()
            .toList();
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
    public boolean harOmsorg() {
        return regelGrunnlag.getRettOgOmsorg().getHarOmsorg();
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
        return isFarRett() && !isMorRett() && regelGrunnlag.getRettOgOmsorg().getMorUføretrygd();
    }

    @Override
    public boolean isSakMedMinsterett() {
        return regelGrunnlag.getKontoer().harSpesialkonto(Spesialkontotype.BARE_FAR_MINSTERETT)
            && regelGrunnlag.getKontoer().getSpesialkontoTrekkdager(Spesialkontotype.BARE_FAR_MINSTERETT) > 0;
    }

    @Override
    public boolean isSakMedDagerUtenAktivitetskrav() {
        return regelGrunnlag.getKontoer().harSpesialkonto(Spesialkontotype.UTEN_AKTIVITETSKRAV)
            && regelGrunnlag.getKontoer().getSpesialkontoTrekkdager(Spesialkontotype.UTEN_AKTIVITETSKRAV) > 0;
    }

    @Override
    public boolean isSakMedRettEtterStartNesteStønadsperiode() {
        return regelGrunnlag.getKontoer().harSpesialkonto(Spesialkontotype.TETTE_FØDSLER)
            && regelGrunnlag.getKontoer().getSpesialkontoTrekkdager(Spesialkontotype.TETTE_FØDSLER) > 0;
    }

    @Override
    public SaldoUtregning getSaldoUtregning() {
        return saldoUtregning;
    }

    @Override
    public List<AnnenpartUttakPeriode> getAnnenPartUttaksperioder() {
        return regelGrunnlag.getAnnenpartUttaksperioder();
    }

    @Override
    public LocalDateTime getAnnenPartSisteSøknadMottattTidspunkt() {
        return Optional.ofNullable(regelGrunnlag.getAnnenPart()).map(AnnenPart::getSisteSøknadMottattTidspunkt).orElse(null);
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
        return regelGrunnlag.getDatoer().getDødsdatoer() == null ? null : regelGrunnlag.getDatoer().getDødsdatoer().getSøkersDødsdato();
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
    public boolean erAktuellPeriodeEtterStartNesteStønadsperiode() {
        var fom = aktuellPeriode.getFom();
        return regelGrunnlag.getDatoer().getStartdatoNesteStønadsperiode().filter(d -> !fom.isBefore(d)).isPresent();
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
        return Optional.ofNullable(regelGrunnlag.getSøknad()).map(Søknad::getMottattTidspunkt).orElse(null);
    }

    @Override
    public boolean kreverBehandlingSammenhengendeUttak() {
        return regelGrunnlag.getBehandling().isKreverSammenhengendeUttak();
    }

    @Override
    public Optional<LukketPeriode> periodeFarRundtFødsel() {
        return Optional.ofNullable(farRundtFødselIntervall);
    }

    @Override
    public Collection<PleiepengerPeriode> perioderMedPleiepenger() {
        return regelGrunnlag.getYtelser().pleiepenger().map(p -> p.perioder()).orElse(List.of());
    }
}
