package umbra.render.sprite;

import umbra.render.debug.DebugColor;

import java.util.Objects;

/**
 * Sprite draw command with source and destination rectangles in pixels.
 */
public record SpriteDrawCommand(
        String textureId,
        int sourceX,
        int sourceY,
        int sourceWidth,
        int sourceHeight,
        float x,
        float y,
        float width,
        float height,
        boolean flipX,
        boolean flipY,
        DebugColor tint
) {
    public SpriteDrawCommand {
        if (textureId == null || textureId.isBlank()) {
            throw new IllegalArgumentException("textureId must not be blank");
        }
        if (sourceWidth <= 0 || sourceHeight <= 0) {
            throw new IllegalArgumentException("source size must be positive");
        }
        if (width <= 0.0f || height <= 0.0f) {
            throw new IllegalArgumentException("destination size must be positive");
        }
        Objects.requireNonNull(tint, "tint must not be null");
    }
}
