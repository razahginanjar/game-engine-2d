package umbra.save;

public final class SaveValidationException extends RuntimeException {
    public SaveValidationException(String message) {
        super(message);
    }

    public SaveValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
