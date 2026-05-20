package umbra.project;

public final class GameManifestValidationException extends RuntimeException {
    public GameManifestValidationException(String message) {
        super(message);
    }

    public GameManifestValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
