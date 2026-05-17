package umbra.render.debug;

/**
 * RGBA color with normalized channels.
 */
public record DebugColor(float r, float g, float b, float a) {
    public DebugColor {
        validateChannel("r", r);
        validateChannel("g", g);
        validateChannel("b", b);
        validateChannel("a", a);
    }

    private static void validateChannel(String channel, float value) {
        if (value < 0.0f || value > 1.0f) {
            throw new IllegalArgumentException(channel + " must be between 0 and 1");
        }
    }
}
