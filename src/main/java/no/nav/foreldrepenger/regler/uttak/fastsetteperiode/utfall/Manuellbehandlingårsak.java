package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall;

public enum Manuellbehandlingårsak {
    STØNADSKONTO_TOM(5001, "Stønadskonto tom for stønadsdager"),
    UGYLDIG_STØNADSKONTO(5002, "Ugyldig stønadskonto"),
    AVKLAR_ARBEID(5006, "Avklar arbeid"),
    SØKNADSFRIST(5010, "Uttak ikke gyldig pga søknadsfrist"),
    IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE(5011, "Ikke gyldig grunn for utsettelse"),
    AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT(5004, "Aktivitetskravet må sjekkes manuelt"),
    VURDER_SAMTIDIG_UTTAK(5014, "Vurder samtidig uttak"),
    STEBARNSADOPSJON(5019, "Stebarnsadopsjon - sjekk uttak i forhold til aktivitetskravet"),
    OPPHOLD_STØRRE_ENN_TILGJENGELIGE_DAGER(5024, "Opphold større enn tilgjengelige dager"),
    IKKE_HELTIDSARBEID(5025, "Utsettelse ugyldig da søker ikke er i heltidsarbeid"),
    DØDSFALL(5026, "Vurder uttak med hensyn på dødsfall"),
    MOR_UFØR(5027, "Mor er ufør"),
    OVERLAPPENDE_PLEIEPENGER_MED_INNLEGGELSE(5028, "Innvilget pleiepenger med innleggelse, vurder riktig ytelse"),
    OVERLAPPENDE_PLEIEPENGER_UTEN_INNLEGGELSE(5029, "Innvilget pleiepenger uten innleggelse, vurder riktig ytelse"),
    FAR_SØKER_FØR_FØDSEL(5030, "Far/medmor periode før fødsel/omsorg"),
    VURDER_OM_UTSETTELSE(5031, "Vurder om det skal være utsettelse i perioden"),
    AKTIVITETSKRAV_DELVIS_ARBEID(5032, "Mor jobber under 75 prosent");

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
