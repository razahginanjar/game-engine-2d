package umbra.render.debug;

import java.util.Objects;

/**
 * Debug rectangle command in world pixels.
 */
public record DebugRect(
        float x,
        float y,
        float width,
        float height,
        DebugColor color,
        DebugShapeStyle style
) {
    public DebugRect {
        if (width <= 0.0f || height <= 0.0f) {
            throw new IllegalArgumentException("debug rect size must be positive");
        }
        Objects.requireNonNull(color, "color must not be null");
        if (style == null) {
            style = DebugShapeStyle.LINE;
        }
    }
}
