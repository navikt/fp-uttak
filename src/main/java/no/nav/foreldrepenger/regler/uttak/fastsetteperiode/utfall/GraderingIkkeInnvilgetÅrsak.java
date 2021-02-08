package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall;

public enum GraderingIkkeInnvilgetÅrsak {

    AVSLAG_PGA_SEN_SØKNAD(4501, "For sen søknad"),
    AVSLAG_PGA_FOR_TIDLIG_GRADERING(4504, "Gradering før uke 7");

    private final int id;
    private final String beskrivelse;

    GraderingIkkeInnvilgetÅrsak(int id, String beskrivelse) {
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

