package no.nav.foreldrepenger.regler.uttak.beregnkontoer;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.BeregnMinsterettGrunnlag;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.Dekningsgrad;

public enum Minsterett {

    GENERELL_MINSTERETT,
    FAR_UTTAK_RUNDT_FØDSEL,
    UTEN_AKTIVITETSKRAV;

    //TODO Inn i konfig
    public static final int MINSTEDAGER_UFØRE_100_PROSENT = 75;
    public static final int MINSTEDAGER_UFØRE_80_PROSENT = 95;

    public static final int MOR_TO_TETTE_MINSTERETT_DAGER = 110;
    public static final int FAR_TO_TETTE_MINSTERETT_DAGER = 40;
    public static final int BFHR_MINSTERETT_DAGER = 40;
    public static final int UTTAK_RUNDT_FØDSEL_DAGER = 10;

    public static final int TO_TETTE_UKER_MELLOM_FAMHENDELSE = 48;

    public static Map<Minsterett, Integer> finnMinsterett(BeregnMinsterettGrunnlag grunnlag) {

        var toTette = toTette(grunnlag.getFamilieHendelseDato(), grunnlag.getFamilieHendelseDatoNesteSak());
        var retter = new HashMap<Minsterett, Integer>();
        var minsterett = grunnlag.isMinsterett();
        var morHarUføretrygd = grunnlag.isMorHarUføretrygd();
        var bareFarHarRett = grunnlag.isBareFarHarRett();
        var aleneomsorg = grunnlag.isAleneomsorg();
        if (minsterett) {
            var antallDager = 0;
            if (toTette) {
                // Begge skal ha minsterett
                antallDager = grunnlag.isMor() ? MOR_TO_TETTE_MINSTERETT_DAGER : FAR_TO_TETTE_MINSTERETT_DAGER;
            }
            if (bareFarHarRett && !aleneomsorg) {
                antallDager = toTette ? Math.max(BFHR_MINSTERETT_DAGER, FAR_TO_TETTE_MINSTERETT_DAGER) : BFHR_MINSTERETT_DAGER;
            }
            if (morHarUføretrygd && bareFarHarRett && !aleneomsorg) {
                antallDager = Dekningsgrad.DEKNINGSGRAD_80.equals(grunnlag.getDekningsgrad()) ? MINSTEDAGER_UFØRE_80_PROSENT : MINSTEDAGER_UFØRE_100_PROSENT;
            }
            if (antallDager > 0) {
                retter.put(Minsterett.GENERELL_MINSTERETT, antallDager);
            }
            // Settes for begge. Brukes ifm berørt for begge og fakta uttak for far.
            retter.put(Minsterett.FAR_UTTAK_RUNDT_FØDSEL, UTTAK_RUNDT_FØDSEL_DAGER);
        } else if (morHarUføretrygd && bareFarHarRett && !aleneomsorg) {
            var antallDager = Dekningsgrad.DEKNINGSGRAD_80.equals(grunnlag.getDekningsgrad()) ? MINSTEDAGER_UFØRE_80_PROSENT : MINSTEDAGER_UFØRE_100_PROSENT;
            retter.put(Minsterett.UTEN_AKTIVITETSKRAV, antallDager);
        }
        return retter;
    }


    private static boolean toTette(LocalDate familieHendelseDato, LocalDate familieHendelseDatoNesteSak) {
        if (familieHendelseDato == null || familieHendelseDatoNesteSak == null) {
            return false;
        }
        var grenseToTette = familieHendelseDato.plus(Period.ofWeeks(TO_TETTE_UKER_MELLOM_FAMHENDELSE)).plusDays(1);
        return grenseToTette.isAfter(familieHendelseDatoNesteSak);
    }
}
