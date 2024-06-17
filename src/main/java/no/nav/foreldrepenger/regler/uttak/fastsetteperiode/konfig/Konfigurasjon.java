package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.konfig;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class Konfigurasjon {

    public static final Konfigurasjon STANDARD;

    static final LocalDate PREMATURUKER_REGELENDRING_START_DATO = LocalDate.of(2019, 7, 1);

    static {
        var d_2010_01_01 = LocalDate.of(2010, Month.JANUARY, 1);
        var d_2017_01_01 = LocalDate.of(2017, Month.JANUARY, 1);
        var d_2019_07_01 = LocalDate.of(2019, Month.JULY, 1);
        var d_2022_08_02 = LocalDate.of(2022, Month.AUGUST, 1);
        STANDARD = KonfigurasjonBuilder.create()
            // Uttaksperioder
            .leggTilParameter(Parametertype.FORBEHOLDT_MOR_ETTER_FØDSEL_UKER, d_2010_01_01, null, 6)
            .leggTilParameter(Parametertype.TIDLIGST_UTTAK_FØR_TERMIN_UKER, d_2010_01_01, null, 12)
            .leggTilParameter(Parametertype.SENEST_UTTAK_FØR_TERMIN_UKER, d_2010_01_01, null, 3)
            .leggTilParameter(Parametertype.UTTAK_ETTER_BARN_DØDT_UKER, d_2017_01_01, null, 6)
            .leggTilParameter(Parametertype.FAR_UTTAK_FØR_TERMIN_UKER, d_2017_01_01, null, 2) // TODO: endre til aug 2022 etter overgang
            .leggTilParameter(Parametertype.FAR_UTTAK_ETTER_FØDSEL_UKER, d_2017_01_01, null, 6) // TODO: endre til aug 2022 etter overgang

            // Grenser
            .leggTilParameter(Parametertype.GRENSE_ETTER_FØDSELSDATO_ÅR, d_2010_01_01, null, 3)
            .leggTilParameter(Parametertype.TETTE_SAKER_MELLOMROM_UKER, d_2017_01_01, null,
                48)  // TODO: endre til aug 2022 el 48 uker tidligere etter overgang
            .leggTilParameter(Parametertype.PREMATURUKER_ANTALL_DAGER_FØR_TERMIN, d_2019_07_01, null, 52)
            .build();
    }

    private final Map<Parametertype, Collection<Parameter>> parameterMap = new EnumMap<>(Parametertype.class);

    Konfigurasjon(Map<Parametertype, Collection<Parameter>> parameterMap) {
        this.parameterMap.putAll(parameterMap);
    }

    public Optional<Integer> getParameterHvisAktivVed(Parametertype parametertype, final LocalDate dato) {
        return Optional.ofNullable(this.parameterMap.get(parametertype))
            .flatMap(param -> param.stream().filter(p -> p.overlapper(dato)).findFirst().map(Parameter::getVerdi));
    }

    public Integer getParameter(Parametertype parametertype, final LocalDate dato) {
        return getParameterVerdier(parametertype).stream()
            .filter(p -> p.overlapper(dato))
            .findFirst()
            .map(Parameter::getVerdi)
            .orElseThrow(() -> new IllegalArgumentException("Ingen parameter funnet for " + parametertype.name() + " på dato " + dato));
    }

    public Collection<Parameter> getParameterVerdier(Parametertype parametertype) {
        return Optional.ofNullable(this.parameterMap.get(parametertype))
            .orElseThrow(() -> new IllegalArgumentException("Konfigurasjon-feil/Utvikler-feil: mangler parameter av type " + parametertype));
    }

}
