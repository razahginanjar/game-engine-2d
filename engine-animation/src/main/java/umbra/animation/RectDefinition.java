package umbra.animation;

/**
 * Rectangle in frame-local pixels.
 */
public record RectDefinition(int x, int y, int width, int height) {
    public RectDefinition {
        if (width <= 0 || height <= 0) {
            throw new AnimationValidationException("rect size must be positive");
        }
    }
}
