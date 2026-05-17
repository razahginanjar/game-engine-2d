package umbra.render.debug;

import java.util.Objects;

/**
 * Debug line command in world pixels.
 */
public record DebugLine(float x1, float y1, float x2, float y2, DebugColor color) {
    public DebugLine {
        Objects.requireNonNull(color, "color must not be null");
    }
}
