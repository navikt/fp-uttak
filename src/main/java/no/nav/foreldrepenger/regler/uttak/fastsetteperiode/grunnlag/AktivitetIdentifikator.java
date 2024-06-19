package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.Objects;

public class AktivitetIdentifikator {

    private final AktivitetType aktivitetType;
    private final String arbeidsforholdId;
    private final ArbeidsgiverIdentifikator arbeidsgiverIdentifikator;

    private AktivitetIdentifikator(
            AktivitetType aktivitetType, ArbeidsgiverIdentifikator arbeidsgiverIdentifikator, String arbeidsforholdId) {
        this.aktivitetType = aktivitetType;
        this.arbeidsforholdId = arbeidsforholdId;
        this.arbeidsgiverIdentifikator = arbeidsgiverIdentifikator;
    }

    private AktivitetIdentifikator(AktivitetType aktivitetType) {
        this(aktivitetType, null, null);
    }

    public static AktivitetIdentifikator forArbeid(
            ArbeidsgiverIdentifikator arbeidsgiverIdentifikator, String arbeidsforholdId) {
        return new AktivitetIdentifikator(AktivitetType.ARBEID, arbeidsgiverIdentifikator, arbeidsforholdId);
    }

    public static AktivitetIdentifikator forSelvstendigNæringsdrivende() {
        return new AktivitetIdentifikator(AktivitetType.SELVSTENDIG_NÆRINGSDRIVENDE, null, null);
    }

    public static AktivitetIdentifikator forFrilans() {
        return new AktivitetIdentifikator(AktivitetType.FRILANS, null, null);
    }

    public static AktivitetIdentifikator annenAktivitet() {
        return new AktivitetIdentifikator(AktivitetType.ANNET);
    }

    public AktivitetType getAktivitetType() {
        return aktivitetType;
    }

    public ArbeidsgiverIdentifikator getArbeidsgiverIdentifikator() {
        return arbeidsgiverIdentifikator;
    }

    public String getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (AktivitetIdentifikator) o;
        return aktivitetType == that.aktivitetType
                && Objects.equals(arbeidsgiverIdentifikator, that.arbeidsgiverIdentifikator)
                && Objects.equals(arbeidsforholdId, that.arbeidsforholdId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktivitetType, arbeidsgiverIdentifikator, arbeidsforholdId);
    }

    @Override
    public String toString() {
        return "AktivitetIdentifikator{"
                + "aktivitetType="
                + aktivitetType
                + ", arbeidsforholdId='"
                + arbeidsforholdId
                + '\''
                + ", arbeidsgiverIdentifikator="
                + arbeidsgiverIdentifikator
                + '}';
    }
}
