package no.nav.foreldrepenger.regler.uttak.konfig;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class Konfigurasjon {

    public static final Konfigurasjon STANDARD;

    public static final LocalDate PREMATURUKER_REGELENDRING_START_DATO = LocalDate.of(2019, 7, 1);

    static {
        var d_2010_01_01 = LocalDate.of(2010, Month.JANUARY, 1);
        var d_2017_01_01 = LocalDate.of(2017, Month.JANUARY, 1);
        var d_2018_12_31 = LocalDate.of(2018, Month.DECEMBER, 31);
        var d_2019_01_01 = LocalDate.of(2019, Month.JANUARY, 1);
        var d_2019_07_01 = LocalDate.of(2019, Month.JULY, 1);
        var d_2022_08_02 = LocalDate.of(2022, Month.AUGUST, 1);
        STANDARD = KonfigurasjonBuilder.create()
            /*
             * Stønadskontoer
             * - Endring av kvoter/80% fom 1/1-2019
             * - Prematuruker 1/7-2019
             * - FAB 2/8-2022
             */
            // Stønadskontoer
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

            // Ekstradager og Minsteretter
            .leggTilParameter(Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_100, d_2010_01_01, null, 85)
            .leggTilParameter(Parametertype.EKSTRA_DAGER_TO_BARN_FOR_DEKNINGSGRAD_80, d_2010_01_01, null, 105)
            .leggTilParameter(Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_100, d_2010_01_01, null, 230)
            .leggTilParameter(Parametertype.EKSTRA_DAGER_TRE_ELLER_FLERE_BARN_FOR_DEKNINGSGRAD_80, d_2010_01_01, null, 280)
            .leggTilParameter(Parametertype.BARE_FAR_MOR_UFØR_DAGER_UTEN_AKTIVITETSKRAV_100_PROSENT, d_2010_01_01, null, 75)
            .leggTilParameter(Parametertype.BARE_FAR_MOR_UFØR_DAGER_UTEN_AKTIVITETSKRAV_80_PROSENT, d_2010_01_01, null, 95)
            .leggTilParameter(Parametertype.BARE_FAR_DAGER_MINSTERETT, d_2017_01_01, null, 40) // TODO: endre til aug 2022 etter overgang
            .leggTilParameter(Parametertype.BARE_FAR_MOR_UFØR_DAGER_MINSTERETT_100_PROSENT, d_2017_01_01, null, 75) // TODO: endre til aug 2022 etter overgang
            .leggTilParameter(Parametertype.BARE_FAR_MOR_UFØR_DAGER_MINSTERETT_80_PROSENT, d_2017_01_01, null, 95) // TODO: endre til aug 2022 etter overgang
            .leggTilParameter(Parametertype.FAR_DAGER_RUNDT_FØDSEL, d_2017_01_01, null, 10) // TODO: endre til aug 2022 etter overgang
            .leggTilParameter(Parametertype.MOR_TETTE_SAKER_DAGER_FØDSEL, d_2017_01_01, null, 110) // TODO: endre til aug 2022 etter overgang
            .leggTilParameter(Parametertype.MOR_TETTE_SAKER_DAGER_ADOPSJON, d_2017_01_01, null, 40) // TODO: endre til aug 2022 etter overgang
            .leggTilParameter(Parametertype.FAR_TETTE_SAKER_DAGER_MINSTERETT, d_2017_01_01, null, 40) // TODO: endre til aug 2022 etter overgang

            // Uttaksperioder
            .leggTilParameter(Parametertype.UTTAK_MØDREKVOTE_ETTER_FØDSEL_UKER, d_2010_01_01, null, 6)
            .leggTilParameter(Parametertype.LOVLIG_UTTAK_FØR_FØDSEL_UKER, d_2010_01_01, null, 12)
            .leggTilParameter(Parametertype.UTTAK_FELLESPERIODE_FØR_FØDSEL_UKER, d_2010_01_01, null, 3)
            .leggTilParameter(Parametertype.UTTAK_ETTER_BARN_DØDT_UKER, d_2017_01_01, null, 6)
            .leggTilParameter(Parametertype.FAR_UTTAK_FØR_TERMIN_UKER, d_2017_01_01, null, 2) // TODO: endre til aug 2022 etter overgang
            .leggTilParameter(Parametertype.FAR_UTTAK_ETTER_FØDSEL_UKER, d_2017_01_01, null, 6) // TODO: endre til aug 2022 etter overgang

            // Grenser
            .leggTilParameter(Parametertype.GRENSE_ETTER_FØDSELSDATO_ÅR, d_2010_01_01, null, 3)
            .leggTilParameter(Parametertype.TETTE_SAKER_MELLOMROM_UKER, d_2017_01_01, null, 48)  // TODO: endre til aug 2022 el 48 uker tidligere etter overgang
            .leggTilParameter(Parametertype.PREMATURUKER_ANTALL_DAGER_FØR_TERMIN, d_2019_07_01, null, 52)
            .build();
    }

    private final Map<Parametertype, ParameterVerdier> parameterMap = new EnumMap<>(Parametertype.class);

    Konfigurasjon(Map<Parametertype, Collection<Parameter>> parameterMap) {
        for (var entry : parameterMap.entrySet()) {
            this.parameterMap.put(entry.getKey(), new ParameterVerdier(entry.getKey(), entry.getValue()));
        }
    }

    public Optional<Integer> getParameterHvisAktivVed(Parametertype parametertype, final LocalDate dato) {
        return Optional.ofNullable(this.parameterMap.get(parametertype))
                .flatMap(p -> p.getParameterHvisAktivVed(dato));
    }

    public Integer getParameter(Parametertype parametertype, final LocalDate dato) {
        return getParameterVerdier(parametertype).getParameter(dato);
    }

    public ParameterVerdier getParameterVerdier(Parametertype parametertype) {
        return Optional.ofNullable(this.parameterMap.get(parametertype))
                .orElseThrow(() -> new IllegalArgumentException("Konfigurasjon-feil/Utvikler-feil: mangler parameter av type " + parametertype));
    }

}
