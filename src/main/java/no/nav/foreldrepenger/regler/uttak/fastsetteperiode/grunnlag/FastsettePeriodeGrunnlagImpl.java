package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Periode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class FastsettePeriodeGrunnlagImpl implements FastsettePeriodeGrunnlag {

    private final RegelGrunnlag regelGrunnlag;

    private final Trekkdagertilstand trekkdagertilstand;

    private final UttakPeriode aktuellPeriode;

    public FastsettePeriodeGrunnlagImpl(RegelGrunnlag regelGrunnlag,
                                        Trekkdagertilstand trekkdagertilstand,
                                        UttakPeriode aktuellPeriode) {
        this.regelGrunnlag = regelGrunnlag;
        this.trekkdagertilstand = trekkdagertilstand;
        this.aktuellPeriode = aktuellPeriode;
    }

    @Override
    public UttakPeriode getAktuellPeriode() {
        return aktuellPeriode;
    }

    @Override
    public List<GyldigGrunnPeriode> getAktuelleGyldigeGrunnPerioder() {
        Dokumentasjon dokumentasjon = regelGrunnlag.getSøknad().getDokumentasjon();
        if (dokumentasjon == null) {
            return Collections.emptyList();
        }
        return dokumentasjon.getGyldigGrunnPerioder().stream()
                .filter(p -> p.overlapper(aktuellPeriode))
                .sorted(Comparator.comparing(Periode::getFom))
                .collect(Collectors.toList());
    }

    @Override
    public List<AktivitetIdentifikator> getAktiviteter() {
        return new ArrayList<>(regelGrunnlag.getKontoer().keySet());
    }

    @Override
    public Arbeidsprosenter getArbeidsprosenter() {
        return regelGrunnlag.getArbeid().getArbeidsprosenter();
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
    public List<PeriodeMedBarnInnlagt> getPerioderMedBarnInnlagt() {
        return regelGrunnlag.getSøknad().getDokumentasjon().getPerioderMedBarnInnlagt();
    }

    @Override
    public List<UttakPeriode> getPerioderMedAnnenForelderInnlagt() {
        return getPerioderMedOverføringÅrsak(OverføringÅrsak.INNLEGGELSE);
    }

    @Override
    public List<UttakPeriode> getPerioderMedAnnenForelderSykdomEllerSkade() {
        return getPerioderMedOverføringÅrsak(OverføringÅrsak.SYKDOM_ELLER_SKADE);
    }

    @Override
    public List<UttakPeriode> getPerioderMedAnnenForelderIkkeRett() {
        return getPerioderMedOverføringÅrsak(OverføringÅrsak.ANNEN_FORELDER_IKKE_RETT);
    }

    @Override
    public List<UttakPeriode> getPerioderMedAleneomsorg() {
        return getPerioderMedOverføringÅrsak(OverføringÅrsak.ALENEOMSORG);
    }

    private List<UttakPeriode> getPerioderMedOverføringÅrsak(OverføringÅrsak årsak) {
        return regelGrunnlag.getSøknad().getUttaksperioder().stream().filter(periode -> Objects.equals(periode.getOverføringÅrsak(), årsak)
                && PeriodeVurderingType.avklart(periode.getPeriodeVurderingType())).collect(Collectors.toList());
    }


    @Override
    public Stønadskontotype getStønadskontotype() {
        return aktuellPeriode.getStønadskontotype();
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
    public List<GyldigGrunnPeriode> getGyldigGrunnPerioder() {
        return regelGrunnlag.getSøknad().getDokumentasjon().getGyldigGrunnPerioder();
    }

    @Override
    public List<PeriodeUtenOmsorg> getPerioderUtenOmsorg() {
        Dokumentasjon dokumentasjon = regelGrunnlag.getSøknad().getDokumentasjon();
        if (dokumentasjon == null) {
            return Collections.emptyList();
        }
        return dokumentasjon.getPerioderUtenOmsorg();
    }

    @Override
    public LocalDate getFørsteLovligeUttaksdag() {
        return regelGrunnlag.getDatoer().getFørsteLovligeUttaksdag();
    }

    @Override
    public boolean erEndringssøknad() {
        return regelGrunnlag.erRevurdering() && regelGrunnlag.getRevurdering().getErEndringssøknad();
    }

    @Override
    public LocalDate getEndringssøknadMottattdato() {
        return regelGrunnlag.getRevurdering().getEndringssøknadMottattdato();
    }

    @Override
    public Trekkdagertilstand getTrekkdagertilstand() {
        return trekkdagertilstand;
    }

    @Override
    public List<AnnenpartUttaksperiode> getAnnenPartUttaksperioder() {
        return regelGrunnlag.getAnnenPart() != null ? regelGrunnlag.getAnnenPart().getUttaksperioder() : Collections.emptyList();
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
    public boolean isTapendeBehandling() {
        return regelGrunnlag.getBehandling().isTapende();
    }
}
