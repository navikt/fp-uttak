package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class Trekkdagertilstand {

    public enum Part {
        SØKER,
        ANNEN_PART
    }
    private final Map<AktivitetIdentifikator, Kontoer> kontoerForAktiviteter;

    private final Map<Part, List<AktivitetIdentifikator>> aktiviteter = new EnumMap<>(Part.class);
    private boolean samtykke;
    private final Map<Part, Forbruk> forbrukteDager = new EnumMap<>(Part.class);
    private List<FastsattPeriodeAnnenPart> annenPartsPerioderSomSkalTrekkes;
    private Trekkdagertilstand(Map<AktivitetIdentifikator, Kontoer> kontoerForAktiviteter,
                              List<AktivitetIdentifikator> søkersAktiviteter,
                              List<AktivitetIdentifikator> annenPartAktiviteter,
                              List<FastsattPeriodeAnnenPart> annenPartsPerioderSomSkalTrekkes,
                              boolean samtykke) {
        this.annenPartsPerioderSomSkalTrekkes = annenPartsPerioderSomSkalTrekkes;
        this.samtykke = samtykke;
        this.kontoerForAktiviteter = kontoerForAktiviteter;
        aktiviteter.put(Part.SØKER, søkersAktiviteter);
        aktiviteter.put(Part.ANNEN_PART, annenPartAktiviteter);
        forbrukteDager.put(Part.SØKER, Forbruk.zero(søkersAktiviteter));
        forbrukteDager.put(Part.ANNEN_PART, Forbruk.zero(annenPartAktiviteter));
    }

    private Trekkdagertilstand(RegelGrunnlag grunnlag, List<FastsattPeriodeAnnenPart> annenPartsPerioderSomSkalTrekkes) {
        this(grunnlag.getKontoer(),
                grunnlag.getKontoer() == null ? Collections.emptyList() : new ArrayList<>(grunnlag.getKontoer().keySet()),
                grunnlag.getAnnenPart() == null ? Collections.emptyList() : grunnlag.getAnnenPart().getAktiviteter(),
                annenPartsPerioderSomSkalTrekkes,
                grunnlag.getRettOgOmsorg() != null && grunnlag.getRettOgOmsorg().getSamtykke());
    }

    public static Trekkdagertilstand forBerørtSak(RegelGrunnlag grunnlag) {
        Trekkdagertilstand trekkdagertilstand = new Trekkdagertilstand(grunnlag, Collections.emptyList());
        for (FastsattPeriodeAnnenPart fastsattPeriodeAnnenPart : grunnlag.getAnnenPart().getUttaksperioder()) {
            trekkdagertilstand.registrerForbrukAnnenPart(fastsattPeriodeAnnenPart);
        }
        return trekkdagertilstand;
    }

    public static Trekkdagertilstand ny(RegelGrunnlag grunnlag, List<UttakPeriode> uttakPerioderSøker) {
        List<FastsattPeriodeAnnenPart> annenPartsPerioderSomSkalTrekkes = knekkUttakPerioderAnnenPartBasertPåUttakPerioderSøker(uttakPerioderSøker,
                grunnlag.getAnnenPart() == null ? Collections.emptyList() : grunnlag.getAnnenPart().getUttaksperioder());
        return new Trekkdagertilstand(grunnlag, annenPartsPerioderSomSkalTrekkes);
    }

    private void registrerForbruk(Part part, UttakPeriode periode) {
        Forbruk forbruk = forbrukteDager.get(part);
        for (AktivitetIdentifikator aktivitet : aktiviteter.get(part)) {
            forbruk.registrerForbruk(aktivitet, periode.getStønadskontotype(), periode.getTrekkdager(aktivitet));
            if (periode.isFlerbarnsdager()) {
                forbruk.registrerForbruk(aktivitet, Stønadskontotype.FLERBARNSDAGER, periode.getTrekkdager(aktivitet));
            }
        }
    }

    private void registrerForbrukAnnenPart(FastsattPeriodeAnnenPart periode) {
        Forbruk forbruk = forbrukteDager.get(Part.ANNEN_PART);
        for (UttakPeriodeAktivitet uttakPeriodeAktivitet : periode.getUttakPeriodeAktiviteter()) {
            if (uttakPeriodeAktivitet.getTrekkdager().merEnn0()) {
                forbruk.registrerForbruk(uttakPeriodeAktivitet.getAktivitetIdentifikator(), uttakPeriodeAktivitet.getStønadskontotype(), uttakPeriodeAktivitet.getTrekkdager());
                if (periode.isFlerbarnsdager()) {
                    forbruk.registrerForbruk(uttakPeriodeAktivitet.getAktivitetIdentifikator(), Stønadskontotype.FLERBARNSDAGER, uttakPeriodeAktivitet.getTrekkdager());
                }
            }
        }
    }

    private Optional<Integer> kvoteForStønadskontotype(Kontoer kontoer, Stønadskontotype stønadskontotype) {
        return kontoer.getKontoList()
                .stream()
                .filter(konto -> Objects.equals(konto.getType(), stønadskontotype))
                .map(Konto::getTrekkdager)
                .findFirst();
    }

    private Trekkdager getSamletForbruk(AktivitetIdentifikator aktivitetIdentifikator, Stønadskontotype stønadskontotype) {
        return forbrukteDager.get(Part.SØKER).getForbruk(aktivitetIdentifikator, stønadskontotype)
                .add(forbrukteDager.get(Part.ANNEN_PART).getMinsteForbruk(stønadskontotype));
    }

    void reduserSaldo(UttakPeriode uttakPeriode) {
        registrerForbruk(Trekkdagertilstand.Part.SØKER, uttakPeriode);
    }

    public void trekkSaldoForAnnenPartsPerioder(UttakPeriode periodeUnderBehandling) {
        List<FastsattPeriodeAnnenPart> førFjern = new ArrayList<>(annenPartsPerioderSomSkalTrekkes);
        for (FastsattPeriodeAnnenPart periodeAnnenPart : annenPartsPerioderSomSkalTrekkes) {
            if (periodeAnnenPart.getTom().isBefore(periodeUnderBehandling.getFom())) {
                registrerForbrukAnnenPart(periodeAnnenPart);
                førFjern.remove(periodeAnnenPart);
            } else if (erLikPeriode(periodeUnderBehandling, periodeAnnenPart)) {
                if (erSamtidigUttak(periodeUnderBehandling, periodeAnnenPart) && samtykke) {
                    registrerForbrukAnnenPart(periodeAnnenPart);
                }
                førFjern.remove(periodeAnnenPart);
            }
        }
        annenPartsPerioderSomSkalTrekkes = førFjern;
    }

    private boolean erSamtidigUttak(UttakPeriode periodeUnderBehandling, FastsattPeriodeAnnenPart periodeAnnenPart) {
        return periodeAnnenPart.isSamtidigUttak() || periodeUnderBehandling.isSamtidigUttak();
    }

    private boolean erLikPeriode(UttakPeriode periodeUnderBehandling, FastsattPeriodeAnnenPart periodeAnnenPart) {
        return erLik(periodeAnnenPart.getFom(), periodeAnnenPart.getTom(), periodeUnderBehandling.getFom(), periodeUnderBehandling.getTom());
    }

    private boolean erLik(LocalDate fom1, LocalDate tom1, LocalDate fom2, LocalDate tom2) {
        return fom1.isEqual(fom2) && tom1.isEqual(tom2);
    }

    private static List<FastsattPeriodeAnnenPart> knekkUttakPerioderAnnenPartBasertPåUttakPerioderSøker(List<UttakPeriode> uttakPerioderSøker,
                                                                                                        List<FastsattPeriodeAnnenPart> uttakPerioderAnnenPart) {
        Set<LocalDate> knekkpunkter = new TreeSet<>();
        for (UttakPeriode periode : uttakPerioderSøker) {
            knekkpunkter.add(periode.getFom());
            knekkpunkter.add(periode.getTom().plusDays(1));
        }

        List<FastsattPeriodeAnnenPart> annenPartPerioder = new ArrayList<>(uttakPerioderAnnenPart);
        for (LocalDate knekkpunkt : knekkpunkter) {
            annenPartPerioder = knekk(annenPartPerioder, knekkpunkt);
        }

        //fjern alle som er etter siste uttaksperiode fra søker
        return annenPartPerioder.stream()
                .filter(annenPart -> annenPart.getTom().isBefore(uttakPerioderSøker.get(uttakPerioderSøker.size() - 1).getFom()))
                .collect(Collectors.toList());
    }

    private static List<FastsattPeriodeAnnenPart> knekk(List<FastsattPeriodeAnnenPart> førKnekk, LocalDate knekkpunkt) {
        List<FastsattPeriodeAnnenPart> etterKnekk = new ArrayList<>();
        for (FastsattPeriodeAnnenPart periode : førKnekk) {
            if (periode.overlapper(knekkpunkt) && !periode.getFom().equals(knekkpunkt)) {
                //Må fordele trekkdager på aktivitetene for annenpart før og etter knekkpunkt så vi kopierer aktivitetene og endret trekkdager
                List<UttakPeriodeAktivitet> aktiviteterForPeriodeFørKnekkpunkt = aktiviteterForPeriodeFørKnekkpunkt(periode, knekkpunkt.minusDays(1));
                List<UttakPeriodeAktivitet> aktiviteterForPeriodeEtterKnekkpunkt = aktiviteterForPeriodeEtterKnekkpunkt(periode.getUttakPeriodeAktiviteter(), aktiviteterForPeriodeFørKnekkpunkt);

                etterKnekk.add(periode.kopiMedNyPeriode(periode.getFom(), knekkpunkt.minusDays(1), aktiviteterForPeriodeFørKnekkpunkt));
                etterKnekk.add(periode.kopiMedNyPeriode(knekkpunkt, periode.getTom(), aktiviteterForPeriodeEtterKnekkpunkt));
            } else {
                etterKnekk.add(periode);
            }
        }

        return etterKnekk;
    }

    private static List<UttakPeriodeAktivitet> aktiviteterForPeriodeFørKnekkpunkt(FastsattPeriodeAnnenPart periode, LocalDate knekkTom) {
        int virkedagerInnenfor = Virkedager.beregnAntallVirkedager(periode.getFom(), knekkTom);
        int virkedagerHele = periode.virkedager();

        List<UttakPeriodeAktivitet> uttakPeriodeAktivitetMedNyttTrekkDager = new ArrayList<>();

        for (UttakPeriodeAktivitet uttakPeriodeAktivitet : periode.getUttakPeriodeAktiviteter()) {
            Trekkdager opprinneligeTrekkdager = uttakPeriodeAktivitet.getTrekkdager();
            if (virkedagerInnenfor > 0 && opprinneligeTrekkdager.merEnn0()) {
                BigDecimal vektetTrekkdager = opprinneligeTrekkdager.decimalValue().multiply(BigDecimal.valueOf(virkedagerInnenfor))
                        .divide(BigDecimal.valueOf(virkedagerHele), 0, RoundingMode.DOWN);
                uttakPeriodeAktivitetMedNyttTrekkDager
                        .add(new UttakPeriodeAktivitet(uttakPeriodeAktivitet.getAktivitetIdentifikator(),
                        uttakPeriodeAktivitet.getStønadskontotype(),
                                new Trekkdager(vektetTrekkdager),
                                uttakPeriodeAktivitet.getUtbetalingsgrad(),
                                uttakPeriodeAktivitet.getGradertArbeidsprosent()));
            }
        }

        return uttakPeriodeAktivitetMedNyttTrekkDager;
    }

    private static List<UttakPeriodeAktivitet> aktiviteterForPeriodeEtterKnekkpunkt(List<UttakPeriodeAktivitet> uttakPeriodeAktiviteter,
                                                                             List<UttakPeriodeAktivitet> aktiviteterForPeriodeFørKnekkpunkt) {
        List<UttakPeriodeAktivitet> uttakPeriodeAktivitetMedNyttTrekkDager = new ArrayList<>();

        for(UttakPeriodeAktivitet uttakPeriodeAktivitet : uttakPeriodeAktiviteter){
            for(UttakPeriodeAktivitet uttakPeriodeAktivitetFørKnekkpunkt: aktiviteterForPeriodeFørKnekkpunkt) {
                if(uttakPeriodeAktivitet.getAktivitetIdentifikator().equals(uttakPeriodeAktivitetFørKnekkpunkt.getAktivitetIdentifikator())) {
                    Trekkdager opprinneligeTrekkdager = uttakPeriodeAktivitet.getTrekkdager();
                    Trekkdager trekkDagerFørKnekkpunkt = uttakPeriodeAktivitetFørKnekkpunkt.getTrekkdager();
                    Trekkdager trekkDagerEtterKnekkpunkt = opprinneligeTrekkdager.subtract(trekkDagerFørKnekkpunkt);
                        uttakPeriodeAktivitetMedNyttTrekkDager
                                .add(new UttakPeriodeAktivitet(uttakPeriodeAktivitet.getAktivitetIdentifikator(),
                                        uttakPeriodeAktivitet.getStønadskontotype(),
                                        trekkDagerEtterKnekkpunkt,
                                        uttakPeriodeAktivitet.getUtbetalingsgrad(),
                                        uttakPeriodeAktivitet.getGradertArbeidsprosent()));
                }
            }

        }
        return uttakPeriodeAktivitetMedNyttTrekkDager;
    }


    /**
     * Finn saldo for gitt stønadskontotype.
     *
     * @param stønadskontotype stønadskontotypen det skal finnes saldo for.
     * @return saldo for gitt stønadskontotype.
     */
    public Trekkdager saldo(AktivitetIdentifikator aktivitetIdentifikator, Stønadskontotype stønadskontotype) {
        return getGjenstående(aktivitetIdentifikator, stønadskontotype);
    }

    private Trekkdager getGjenstående(AktivitetIdentifikator aktivitetIdentifikator, Stønadskontotype stønadskontotype) {
        Kontoer kontoer = kontoerForAktiviteter.get(aktivitetIdentifikator);
        return new Trekkdager(kvoteForStønadskontotype(kontoer, stønadskontotype).orElse(0)).subtract(getSamletForbruk(aktivitetIdentifikator, stønadskontotype));
    }
}
