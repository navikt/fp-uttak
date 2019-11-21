package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class Trekkdagertilstand {

    private final Map<AktivitetIdentifikator, Kontoer> kontoerForAktiviteter;
    private final boolean samtykke;
    private final boolean erMor;
    private final Forbruk forbrukAnnenpart;
    private final Forbruk forbrukSøker;
    private List<AnnenpartUttaksperiode> annenPartsPerioderSomSkalTrekkes;

    private Trekkdagertilstand(Map<AktivitetIdentifikator, Kontoer> kontoerForAktiviteter,
                               List<AktivitetIdentifikator> søkersAktiviteter,
                               List<AktivitetIdentifikator> annenPartAktiviteter,
                               List<AnnenpartUttaksperiode> annenPartsPerioderSomSkalTrekkes,
                               boolean samtykke,
                               boolean erMor) {
        this.annenPartsPerioderSomSkalTrekkes = annenPartsPerioderSomSkalTrekkes;
        this.samtykke = samtykke;
        this.kontoerForAktiviteter = kontoerForAktiviteter;
        this.erMor = erMor;
        forbrukSøker = Forbruk.zero(søkersAktiviteter);
        forbrukAnnenpart = Forbruk.zero(annenPartAktiviteter);
    }

    private Trekkdagertilstand(RegelGrunnlag grunnlag, List<AnnenpartUttaksperiode> annenPartsPerioderSomSkalTrekkes) {
        this(grunnlag.getKontoer(),
                grunnlag.getKontoer() == null ? List.of() : new ArrayList<>(grunnlag.getKontoer().keySet()),
                grunnlag.getAnnenPart() == null ? List.of() : grunnlag.getAnnenPart().getAktiviteter(),
                annenPartsPerioderSomSkalTrekkes,
                grunnlag.getRettOgOmsorg() != null && grunnlag.getRettOgOmsorg().getSamtykke(),
                grunnlag.getBehandling().isSøkerMor());
    }

    public static Trekkdagertilstand forTapendeBehandling(RegelGrunnlag grunnlag, List<UttakPeriode> uttakPerioderSøker) {
        var annenPart = Objects.requireNonNull(grunnlag.getAnnenPart(), "annenpart");
        List<AnnenpartUttaksperiode> annenPartsPerioderSomSkalTrekkes = knekkUttakPerioderAnnenPartBasertPåUttakPerioderSøker(uttakPerioderSøker, annenPart.getUttaksperioder());
        Trekkdagertilstand trekkdagertilstand = new Trekkdagertilstand(grunnlag, List.of());
        for (AnnenpartUttaksperiode annenpartPeriode : annenPartsPerioderSomSkalTrekkes) {
            if (!annenpartPeriode.isOppholdsperiode() || !overlapperMedSøktPeriode(uttakPerioderSøker, annenpartPeriode)) {
                trekkdagertilstand.registrerForbrukAnnenPart(annenpartPeriode);
            }
        }
        return trekkdagertilstand;
    }

    private static boolean overlapperMedSøktPeriode(List<UttakPeriode> uttakPerioderSøker, AnnenpartUttaksperiode annenpartPeriode) {
        return uttakPerioderSøker.stream().anyMatch(søkersPeriode -> søkersPeriode.overlapper(annenpartPeriode));
    }

    public static Trekkdagertilstand ny(RegelGrunnlag grunnlag, List<UttakPeriode> uttakPerioderSøker) {
        List<AnnenpartUttaksperiode> annenPartsPerioderSomSkalTrekkes = knekkUttakPerioderAnnenPartBasertPåUttakPerioderSøker(uttakPerioderSøker,
                grunnlag.getAnnenPart() == null ? List.of() : grunnlag.getAnnenPart().getUttaksperioder());
        return new Trekkdagertilstand(grunnlag, annenPartsPerioderSomSkalTrekkes);
    }

    private void registrerForbruk(Forbruk forbruk, UttakPeriode periode) {
        for (AktivitetIdentifikator aktivitet : forbruk.getAktiviteter()) {
            if (periode.getSluttpunktTrekkerDager(aktivitet)) {
                forbruk.registrerForbruk(aktivitet, periode.getStønadskontotype(), periode.getTrekkdager(aktivitet));
                if (periode.isFlerbarnsdager()) {
                    forbruk.registrerForbruk(aktivitet, Stønadskontotype.FLERBARNSDAGER, periode.getTrekkdager(aktivitet));
                }
            }
        }
    }

    private void registrerForbrukAnnenPart(AnnenpartUttaksperiode periode) {
        if (periode.isOppholdsperiode()) {
            registrerForbrukAnnenpartOpphold(periode);
        } else {
            for (UttakPeriodeAktivitet uttakPeriodeAktivitet : periode.getUttakPeriodeAktiviteter()) {
                forbrukAnnenpart.registrerForbruk(uttakPeriodeAktivitet.getAktivitetIdentifikator(), uttakPeriodeAktivitet.getStønadskontotype(), uttakPeriodeAktivitet.getTrekkdager());
                if (periode.isFlerbarnsdager()) {
                    forbrukAnnenpart.registrerForbruk(uttakPeriodeAktivitet.getAktivitetIdentifikator(), Stønadskontotype.FLERBARNSDAGER, uttakPeriodeAktivitet.getTrekkdager());
                }
            }
        }
    }

    private void registrerForbrukAnnenpartOpphold(AnnenpartUttaksperiode periode) {
        for (AktivitetIdentifikator aktivitet : forbrukAnnenpart.getAktiviteter()) {
            forbrukAnnenpart.registrerForbruk(aktivitet, Oppholdårsaktype.map(periode.getOppholdårsaktype(), !erMor), new Trekkdager(periode.virkedager()));
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
        return forbrukSøker.getForbruk(aktivitetIdentifikator, stønadskontotype).add(forbrukAnnenpart.getMinsteForbruk(stønadskontotype));
    }

    void reduserSaldo(UttakPeriode uttakPeriode) {
        registrerForbruk(forbrukSøker, uttakPeriode);
    }

    public void trekkSaldoForAnnenPartsPerioder(UttakPeriode periodeUnderBehandling) {
        List<AnnenpartUttaksperiode> førFjern = new ArrayList<>(annenPartsPerioderSomSkalTrekkes);
        for (AnnenpartUttaksperiode periodeAnnenPart : annenPartsPerioderSomSkalTrekkes) {
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

    private boolean erSamtidigUttak(UttakPeriode periodeUnderBehandling, AnnenpartUttaksperiode periodeAnnenPart) {
        return periodeAnnenPart.isSamtidigUttak() || periodeUnderBehandling.isSamtidigUttak();
    }

    private boolean erLikPeriode(UttakPeriode periodeUnderBehandling, AnnenpartUttaksperiode periodeAnnenPart) {
        return erLik(periodeAnnenPart.getFom(), periodeAnnenPart.getTom(), periodeUnderBehandling.getFom(), periodeUnderBehandling.getTom());
    }

    private boolean erLik(LocalDate fom1, LocalDate tom1, LocalDate fom2, LocalDate tom2) {
        return fom1.isEqual(fom2) && tom1.isEqual(tom2);
    }

    private static List<AnnenpartUttaksperiode> knekkUttakPerioderAnnenPartBasertPåUttakPerioderSøker(List<UttakPeriode> uttakPerioderSøker,
                                                                                                      List<AnnenpartUttaksperiode> uttakPerioderAnnenPart) {
        List<AnnenpartUttaksperiode> annenPartPerioder = knekkAnnenpartBasertPåSøker(uttakPerioderSøker, uttakPerioderAnnenPart);
        return annenPartPerioder;
    }

    private static List<AnnenpartUttaksperiode> knekkAnnenpartBasertPåSøker(List<UttakPeriode> uttakPerioderSøker, List<AnnenpartUttaksperiode> uttakPerioderAnnenPart) {
        Set<LocalDate> knekkpunkter = new TreeSet<>();
        for (UttakPeriode periode : uttakPerioderSøker) {
            knekkpunkter.add(periode.getFom());
            knekkpunkter.add(periode.getTom().plusDays(1));
        }

        List<AnnenpartUttaksperiode> annenPartPerioder = new ArrayList<>(uttakPerioderAnnenPart);
        for (LocalDate knekkpunkt : knekkpunkter) {
            annenPartPerioder = knekk(annenPartPerioder, knekkpunkt);
        }
        return annenPartPerioder;
    }

    private static List<AnnenpartUttaksperiode> knekk(List<AnnenpartUttaksperiode> førKnekk, LocalDate knekkpunkt) {
        List<AnnenpartUttaksperiode> etterKnekk = new ArrayList<>();
        for (AnnenpartUttaksperiode periode : førKnekk) {
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

    private static List<UttakPeriodeAktivitet> aktiviteterForPeriodeFørKnekkpunkt(AnnenpartUttaksperiode periode, LocalDate knekkTom) {
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

        for (UttakPeriodeAktivitet uttakPeriodeAktivitet : uttakPeriodeAktiviteter) {
            for (UttakPeriodeAktivitet uttakPeriodeAktivitetFørKnekkpunkt : aktiviteterForPeriodeFørKnekkpunkt) {
                if (uttakPeriodeAktivitet.getAktivitetIdentifikator().equals(uttakPeriodeAktivitetFørKnekkpunkt.getAktivitetIdentifikator())) {
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
