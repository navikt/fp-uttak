package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall;

public enum Manuellbehandlingårsak {
    STØNADSKONTO_TOM(5001, "Stønadskonto tom for stønadsdager"),
    UGYLDIG_STØNADSKONTO(5002, "Ugyldig stønadskonto"),
    BEGRUNNELSE_IKKE_GYLDIG(5003, "Begrunnelse ikke gyldig"),
    AVKLAR_ARBEID(5006, "Avklar arbeid"),
    SØKER_HAR_IKKE_OMSORG(5009, "Søker har ikke omsorg for barnet"),
    SØKNADSFRIST(5010, "Uttak ikke gyldig pga søknadsfrist"),
    IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE(5011, "Ikke gyldig grunn for utsettelse"),
    AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT(5004, "Aktivitetskravet må sjekkes manuelt"),
    PERIODE_UAVKLART(5012, "Periode uavklart av saksbehandler"),
    VURDER_SAMTIDIG_UTTAK(5014, "Vurder samtidig uttak"),
    STEBARNSADOPSJON(5019, "Stebarnsadopsjon - sjekk uttak i forhold til aktivitetskravet"),
    OPPHOLD_STØRRE_ENN_TILGJENGELIGE_DAGER(5024, "Opphold større enn tilgjengelige dager"),
    IKKE_HELTIDSARBEID(5025, "Utsettelse ugyldig da søker ikke er i heltidsarbeid"),
    DØDSFALL(5026, "Vurder uttak med hensyn på dødsfall"),
    MOR_UFØR(5027, "Mor er ufør"),
    ;

    private final int id;
    private final String beskrivelse;

    Manuellbehandlingårsak(int id, String beskrivelse) {
        this.id = id;
        this.beskrivelse = beskrivelse;
    }

    public int getId() {
        return id;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }
}
