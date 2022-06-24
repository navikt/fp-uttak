package no.nav.foreldrepenger.regler.uttak.beregnkontoer;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.BeregnMinsterettGrunnlag;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.Dekningsgrad;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;

public enum Minsterett {

    GENERELL_MINSTERETT,
    FAR_UTTAK_RUNDT_FØDSEL,
    TETTE_FØDSLER,
    UTEN_AKTIVITETSKRAV;


    public static Map<Minsterett, Integer> finnMinsterett(BeregnMinsterettGrunnlag grunnlag) {

        var fhDato = grunnlag.getFamilieHendelseDato();
        var toTette = toTette(grunnlag.getFamilieHendelseDato(), grunnlag.getFamilieHendelseDatoNesteSak());
        var retter = new HashMap<Minsterett, Integer>();
        var minsterett = grunnlag.isMinsterett();
        var morHarUføretrygd = grunnlag.isMorHarUføretrygd();
        var bareFarHarRett = grunnlag.isBareFarHarRett();
        var aleneomsorg = grunnlag.isAleneomsorg();
        if (minsterett && toTette) {
            var antallDager = 0;
            if (grunnlag.isMor() && grunnlag.isGjelderFødsel()) {
                antallDager = StandardKonfigurasjon.KONFIGURASJON.getParameter(Parametertype.MOR_TETTE_SAKER_DAGER_FØDSEL, fhDato);
            } else if (grunnlag.isMor()) {
                antallDager = StandardKonfigurasjon.KONFIGURASJON.getParameter(Parametertype.MOR_TETTE_SAKER_DAGER_ADOPSJON, fhDato);
            } else {
                antallDager = StandardKonfigurasjon.KONFIGURASJON.getParameter(Parametertype.FAR_TETTE_SAKER_DAGER_MINSTERETT, fhDato);
            }
            retter.put(TETTE_FØDSLER, antallDager);
        } else if (grunnlag.getFamilieHendelseDatoNesteSak() != null){
            retter.put(TETTE_FØDSLER, 0);
        }
        if (minsterett) {
            // Settes for begge parter. Brukes ifm berørt for begge og fakta uttak for far.
            retter.put(Minsterett.FAR_UTTAK_RUNDT_FØDSEL,
                    StandardKonfigurasjon.KONFIGURASJON.getParameter(Parametertype.FAR_DAGER_RUNDT_FØDSEL, fhDato));
        }
        if (minsterett && bareFarHarRett && !aleneomsorg) {
            var antallDager = StandardKonfigurasjon.KONFIGURASJON.getParameter(Parametertype.BARE_FAR_DAGER_MINSTERETT, fhDato);
            if (morHarUføretrygd) {
                antallDager = Dekningsgrad.DEKNINGSGRAD_80.equals(grunnlag.getDekningsgrad()) ?
                        StandardKonfigurasjon.KONFIGURASJON.getParameter(Parametertype.BARE_FAR_MOR_UFØR_DAGER_MINSTERETT_80_PROSENT, fhDato) :
                        StandardKonfigurasjon.KONFIGURASJON.getParameter(Parametertype.BARE_FAR_MOR_UFØR_DAGER_MINSTERETT_100_PROSENT, fhDato);
            }
            if (grunnlag.getAntallBarn() == 2) {
                antallDager = Dekningsgrad.DEKNINGSGRAD_80.equals(grunnlag.getDekningsgrad()) ?
                        StandardKonfigurasjon.KONFIGURASJON.getParameter(Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_80, fhDato) :
                        StandardKonfigurasjon.KONFIGURASJON.getParameter(Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_100, fhDato);
            }
            if (grunnlag.getAntallBarn() > 2) {
                antallDager = Dekningsgrad.DEKNINGSGRAD_80.equals(grunnlag.getDekningsgrad()) ?
                        StandardKonfigurasjon.KONFIGURASJON.getParameter(Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_80, fhDato) :
                        StandardKonfigurasjon.KONFIGURASJON.getParameter(Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_100, fhDato);
            }
            if (antallDager > 0) {
                retter.put(Minsterett.GENERELL_MINSTERETT, antallDager);
            }
        } else if (morHarUføretrygd && bareFarHarRett && !aleneomsorg) {
            var antallDager = Dekningsgrad.DEKNINGSGRAD_80.equals(grunnlag.getDekningsgrad()) ?
                    StandardKonfigurasjon.KONFIGURASJON.getParameter(Parametertype.BARE_FAR_MOR_UFØR_DAGER_UTEN_AKTIVITETSKRAV_80_PROSENT, fhDato) :
                    StandardKonfigurasjon.KONFIGURASJON.getParameter(Parametertype.BARE_FAR_MOR_UFØR_DAGER_UTEN_AKTIVITETSKRAV_100_PROSENT, fhDato);
            retter.put(Minsterett.UTEN_AKTIVITETSKRAV, antallDager);
        }
        return retter;
    }


    private static boolean toTette(LocalDate familieHendelseDato, LocalDate familieHendelseDatoNesteSak) {
        if (familieHendelseDatoNesteSak == null) {
            return false;
        }
        // TODO: avklare med PE om gjelder for første sak etter WLB eller andre sak etter WLB
        var toTetteGrense = StandardKonfigurasjon.KONFIGURASJON.getParameter(Parametertype.TETTE_SAKER_MELLOMROM_UKER, familieHendelseDato);
        var grenseToTette = familieHendelseDato.plus(Period.ofWeeks(toTetteGrense)).plusDays(1);
        return grenseToTette.isAfter(familieHendelseDatoNesteSak);
    }
}
