package umbra.project;

public final class CreatureDefinitionValidationException extends RuntimeException {
    public CreatureDefinitionValidationException(String message) {
        super(message);
    }

    public CreatureDefinitionValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
