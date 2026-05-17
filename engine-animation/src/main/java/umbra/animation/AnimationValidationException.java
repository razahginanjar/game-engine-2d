package umbra.animation;

public final class AnimationValidationException extends RuntimeException {
    public AnimationValidationException(String message) {
        super(message);
    }

    public AnimationValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
