package umbra.boss;

public final class BossDefinitionValidationException extends RuntimeException {
    public BossDefinitionValidationException(String message) {
        super(message);
    }

    public BossDefinitionValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
