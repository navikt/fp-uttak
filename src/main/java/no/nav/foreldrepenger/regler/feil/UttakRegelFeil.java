package no.nav.foreldrepenger.regler.feil;

public class UttakRegelFeil extends RuntimeException {

    public UttakRegelFeil(String message, Throwable cause) {
        super(message, cause);
    }
}
