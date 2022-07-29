package no.nav.foreldrepenger.regler.uttak.konfig;

import java.time.LocalDate;
import java.time.Month;

@Deprecated(forRemoval = true)
public class StandardKonfigurasjon {

    @Deprecated(forRemoval = true)
    public static final Konfigurasjon KONFIGURASJON;

    //Søknadsdialog trenger støtte før 2019
    @Deprecated(forRemoval = true)
    public static final Konfigurasjon SØKNADSDIALOG;

    static {
        var d_2017_01_01 = LocalDate.of(2017, Month.JANUARY, 1);
        var d_2019_07_01 = LocalDate.of(2019, Month.JULY, 1);
        var d_2022_08_02 = LocalDate.of(2022, Month.AUGUST, 1);
        KONFIGURASJON = KonfigurasjonBuilder.create()
                //Stønadskontoer
                .leggTilParameter(Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_100, d_2017_01_01, null, 85)
                .leggTilParameter(Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_80, d_2017_01_01, null, 105)
                .leggTilParameter(Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_100, d_2017_01_01, null, 230)
                .leggTilParameter(Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_80, d_2017_01_01, null, 280)
                .leggTilParameter(Parametertype.MØDREKVOTE_DAGER_100_PROSENT, d_2017_01_01, null, 75)
                .leggTilParameter(Parametertype.FEDREKVOTE_DAGER_100_PROSENT, d_2017_01_01, null, 75)
                .leggTilParameter(Parametertype.MØDREKVOTE_DAGER_80_PROSENT, d_2017_01_01, null, 95)
                .leggTilParameter(Parametertype.FEDREKVOTE_DAGER_80_PROSENT, d_2017_01_01, null, 95)
                .leggTilParameter(Parametertype.FELLESPERIODE_100_PROSENT_BEGGE_RETT_DAGER, d_2017_01_01, null, 80)
                .leggTilParameter(Parametertype.FELLESPERIODE_80_PROSENT_BEGGE_RETT_DAGER, d_2017_01_01, null, 90)
                .leggTilParameter(Parametertype.FORELDREPENGER_100_PROSENT_MOR_ALENEOMSORG_DAGER, d_2017_01_01, null, 230)
                .leggTilParameter(Parametertype.FORELDREPENGER_80_PROSENT_MOR_ALENEOMSORG_DAGER, d_2017_01_01, null, 280)
                .leggTilParameter(Parametertype.FORELDREPENGER_100_PROSENT_FAR_ALENEOMSORG_DAGER, d_2017_01_01, null, 230)
                .leggTilParameter(Parametertype.FORELDREPENGER_80_PROSENT_FAR_ALENEOMSORG_DAGER, d_2017_01_01, null, 280)
                .leggTilParameter(Parametertype.FORELDREPENGER_100_PROSENT_FAR_HAR_RETT_DAGER, d_2017_01_01, null, 200)
                .leggTilParameter(Parametertype.FORELDREPENGER_80_PROSENT_HAR_RETT_DAGER, d_2017_01_01, null, 250)
                .leggTilParameter(Parametertype.FORELDREPENGER_FØR_FØDSEL, d_2017_01_01, null, 15)
                //Minsteretter
                .leggTilParameter(Parametertype.BARE_FAR_MOR_UFØR_DAGER_UTEN_AKTIVITETSKRAV_100_PROSENT, d_2017_01_01, null, 75)
                .leggTilParameter(Parametertype.BARE_FAR_MOR_UFØR_DAGER_UTEN_AKTIVITETSKRAV_80_PROSENT, d_2017_01_01, null, 95)
                .leggTilParameter(Parametertype.BARE_FAR_DAGER_MINSTERETT, d_2017_01_01, null, 40) // TODO: endre til aug 2022 etter overgang
                .leggTilParameter(Parametertype.BARE_FAR_MOR_UFØR_DAGER_MINSTERETT_100_PROSENT, d_2017_01_01, null, 75) // TODO: endre til aug 2022 etter overgang
                .leggTilParameter(Parametertype.BARE_FAR_MOR_UFØR_DAGER_MINSTERETT_80_PROSENT, d_2017_01_01, null, 95) // TODO: endre til aug 2022 etter overgang
                .leggTilParameter(Parametertype.FAR_DAGER_RUNDT_FØDSEL, d_2017_01_01, null, 10) // TODO: endre til aug 2022 etter overgang
                .leggTilParameter(Parametertype.MOR_TETTE_SAKER_DAGER_FØDSEL, d_2017_01_01, null, 110) // TODO: endre til aug 2022 etter overgang
                .leggTilParameter(Parametertype.MOR_TETTE_SAKER_DAGER_ADOPSJON, d_2017_01_01, null, 40) // TODO: endre til aug 2022 etter overgang
                .leggTilParameter(Parametertype.FAR_TETTE_SAKER_DAGER_MINSTERETT, d_2017_01_01, null, 40) // TODO: endre til aug 2022 etter overgang
                //Uttaksperioder
                .leggTilParameter(Parametertype.UTTAK_MØDREKVOTE_ETTER_FØDSEL_UKER, d_2017_01_01, null, 6)
                .leggTilParameter(Parametertype.LOVLIG_UTTAK_FØR_FØDSEL_UKER, d_2017_01_01, null, 12)
                .leggTilParameter(Parametertype.UTTAK_FELLESPERIODE_FØR_FØDSEL_UKER, d_2017_01_01, null, 3)
                .leggTilParameter(Parametertype.UTTAK_ETTER_BARN_DØDT_UKER, d_2017_01_01, null, 6)
                .leggTilParameter(Parametertype.FAR_UTTAK_FØR_TERMIN_UKER, d_2017_01_01, null, 2) // TODO: endre til aug 2022 etter overgang
                .leggTilParameter(Parametertype.FAR_UTTAK_ETTER_FØDSEL_UKER, d_2017_01_01, null, 6) // TODO: endre til aug 2022 etter overgang
                //grenser
                .leggTilParameter(Parametertype.GRENSE_ETTER_FØDSELSDATO_ÅR, d_2017_01_01, null, 3)
                .leggTilParameter(Parametertype.TETTE_SAKER_MELLOMROM_UKER, d_2017_01_01, null, 48)  // TODO: endre til aug 2022 el 48 uker tidligere etter overgang
                .leggTilParameter(Parametertype.PREMATURUKER_ANTALL_DAGER_FØR_TERMIN, d_2019_07_01, null, 52)
                .build();
        var d_2010_01_01 = LocalDate.of(2010, Month.JANUARY, 1);
        var d_2018_12_31 = LocalDate.of(2018, Month.DECEMBER, 31);
        var d_2019_01_01 = LocalDate.of(2019, Month.JANUARY, 1);
        SØKNADSDIALOG = KonfigurasjonBuilder.create()
                /*
                 * Stønadskontoer
                 * - endring av kvoter/80% fom 1/1-2019
                 * - prematuruker 1/7-2019
                 */
                .leggTilParameter(Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_100, d_2010_01_01, null, 85)
                .leggTilParameter(Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_80, d_2010_01_01, null, 105)
                .leggTilParameter(Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_100, d_2010_01_01, null, 230)
                .leggTilParameter(Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_80, d_2010_01_01, null, 280)
                .leggTilParameter(Parametertype.MØDREKVOTE_DAGER_100_PROSENT, d_2010_01_01, null, 75)
                .leggTilParameter(Parametertype.FEDREKVOTE_DAGER_100_PROSENT, d_2010_01_01, null, 75)
                .leggTilParameter(Parametertype.MØDREKVOTE_DAGER_80_PROSENT, d_2019_01_01, null, 95)
                .leggTilParameter(Parametertype.FEDREKVOTE_DAGER_80_PROSENT, d_2019_01_01, null, 95)
                .leggTilParameter(Parametertype.MØDREKVOTE_DAGER_80_PROSENT, d_2010_01_01, d_2018_12_31, 75)
                .leggTilParameter(Parametertype.FEDREKVOTE_DAGER_80_PROSENT, d_2010_01_01, d_2018_12_31, 75)

                .leggTilParameter(Parametertype.FELLESPERIODE_100_PROSENT_BEGGE_RETT_DAGER, d_2010_01_01, null, 80)
                .leggTilParameter(Parametertype.FELLESPERIODE_80_PROSENT_BEGGE_RETT_DAGER, d_2019_01_01, null, 90)
                .leggTilParameter(Parametertype.FELLESPERIODE_80_PROSENT_BEGGE_RETT_DAGER, d_2010_01_01, d_2018_12_31, 130)
                .leggTilParameter(Parametertype.FORELDREPENGER_100_PROSENT_MOR_ALENEOMSORG_DAGER, d_2010_01_01, null, 230)
                .leggTilParameter(Parametertype.FORELDREPENGER_80_PROSENT_MOR_ALENEOMSORG_DAGER, d_2010_01_01, null, 280)
                .leggTilParameter(Parametertype.FORELDREPENGER_100_PROSENT_FAR_ALENEOMSORG_DAGER, d_2010_01_01, null, 230)
                .leggTilParameter(Parametertype.FORELDREPENGER_80_PROSENT_FAR_ALENEOMSORG_DAGER, d_2010_01_01, null, 280)
                .leggTilParameter(Parametertype.FORELDREPENGER_100_PROSENT_FAR_HAR_RETT_DAGER, d_2010_01_01, null, 200)
                .leggTilParameter(Parametertype.FORELDREPENGER_80_PROSENT_HAR_RETT_DAGER, d_2010_01_01, null, 250)
                .leggTilParameter(Parametertype.FORELDREPENGER_FØR_FØDSEL, d_2010_01_01, null, 15)
                //Minsteretter
                .leggTilParameter(Parametertype.BARE_FAR_MOR_UFØR_DAGER_UTEN_AKTIVITETSKRAV_100_PROSENT, d_2010_01_01, null, 75)
                .leggTilParameter(Parametertype.BARE_FAR_MOR_UFØR_DAGER_UTEN_AKTIVITETSKRAV_80_PROSENT, d_2010_01_01, null, 95)
                .leggTilParameter(Parametertype.BARE_FAR_DAGER_MINSTERETT, d_2017_01_01, null, 40) // TODO: endre til aug 2022 etter overgang
                .leggTilParameter(Parametertype.BARE_FAR_MOR_UFØR_DAGER_MINSTERETT_100_PROSENT, d_2017_01_01, null, 75) // TODO: endre til aug 2022 etter overgang
                .leggTilParameter(Parametertype.BARE_FAR_MOR_UFØR_DAGER_MINSTERETT_80_PROSENT, d_2017_01_01, null, 95) // TODO: endre til aug 2022 etter overgang
                .leggTilParameter(Parametertype.FAR_DAGER_RUNDT_FØDSEL, d_2017_01_01, null, 10) // TODO: endre til aug 2022 etter overgang
                .leggTilParameter(Parametertype.MOR_TETTE_SAKER_DAGER_FØDSEL, d_2017_01_01, null, 110) // TODO: endre til aug 2022 etter overgang
                .leggTilParameter(Parametertype.MOR_TETTE_SAKER_DAGER_ADOPSJON, d_2017_01_01, null, 40) // TODO: endre til aug 2022 etter overgang
                .leggTilParameter(Parametertype.FAR_TETTE_SAKER_DAGER_MINSTERETT, d_2017_01_01, null, 40) // TODO: endre til aug 2022 etter overgang

                //Uttaksperioder
                .leggTilParameter(Parametertype.UTTAK_MØDREKVOTE_ETTER_FØDSEL_UKER, d_2010_01_01, null, 6)
                .leggTilParameter(Parametertype.LOVLIG_UTTAK_FØR_FØDSEL_UKER, d_2010_01_01, null, 12)
                .leggTilParameter(Parametertype.UTTAK_FELLESPERIODE_FØR_FØDSEL_UKER, d_2010_01_01, null, 3)
                .leggTilParameter(Parametertype.UTTAK_ETTER_BARN_DØDT_UKER, d_2017_01_01, null, 6)
                .leggTilParameter(Parametertype.FAR_UTTAK_FØR_TERMIN_UKER, d_2017_01_01, null, 2) // TODO: endre til aug 2022 etter overgang
                .leggTilParameter(Parametertype.FAR_UTTAK_ETTER_FØDSEL_UKER, d_2017_01_01, null, 6) // TODO: endre til aug 2022 etter overgang
                //grenser
                .leggTilParameter(Parametertype.GRENSE_ETTER_FØDSELSDATO_ÅR, d_2010_01_01, null, 3)
                .leggTilParameter(Parametertype.TETTE_SAKER_MELLOMROM_UKER, d_2017_01_01, null, 48)  // TODO: endre til aug 2022 el 48 uker tidligere etter overgang
                .leggTilParameter(Parametertype.PREMATURUKER_ANTALL_DAGER_FØR_TERMIN, d_2019_07_01, null, 52)
                .build();
    }

    private StandardKonfigurasjon() {
        //For å hindre instanser.
    }

}
