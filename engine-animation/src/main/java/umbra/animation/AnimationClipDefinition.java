package umbra.animation;

import java.util.List;

/**
 * Validated clip metadata. A clip can reference a horizontal sheet texture or
 * an explicit texture id sequence for per-frame image assets.
 */
public record AnimationClipDefinition(
        String id,
        String textureId,
        List<String> textureIds,
        int sourceX,
        int sourceY,
        int frameWidth,
        int frameHeight,
        int frameCount,
        float fps,
        boolean loop
) {
    public AnimationClipDefinition {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        textureIds = textureIds == null ? List.of() : List.copyOf(textureIds);
        boolean hasSheetTexture = textureId != null && !textureId.isBlank();
        boolean hasFrameTextures = !textureIds.isEmpty();
        for (String frameTextureId : textureIds) {
            if (frameTextureId == null || frameTextureId.isBlank()) {
                throw new IllegalArgumentException("textureIds must not contain blank values");
            }
        }
        if (!hasSheetTexture && !hasFrameTextures) {
            throw new IllegalArgumentException("clip must define textureId or textureIds");
        }
        if (hasFrameTextures && textureIds.size() != frameCount) {
            throw new IllegalArgumentException("textureIds size must match frameCount");
        }
        if (sourceX < 0 || sourceY < 0) {
            throw new IllegalArgumentException("source position must not be negative");
        }
        if (frameWidth <= 0 || frameHeight <= 0) {
            throw new IllegalArgumentException("frame size must be positive");
        }
        if (frameCount <= 0) {
            throw new IllegalArgumentException("frameCount must be positive");
        }
        if (fps <= 0.0f) {
            throw new IllegalArgumentException("fps must be positive");
        }
    }

    public String textureIdForFrame(int frameIndex) {
        validateFrameIndex(frameIndex);
        if (!textureIds.isEmpty()) {
            return textureIds.get(frameIndex);
        }
        return textureId;
    }

    public int sourceXForFrame(int frameIndex) {
        validateFrameIndex(frameIndex);
        if (!textureIds.isEmpty()) {
            return sourceX;
        }
        return sourceX + frameIndex * frameWidth;
    }

    public int sourceYForFrame(int frameIndex) {
        validateFrameIndex(frameIndex);
        return sourceY;
    }

    private void validateFrameIndex(int frameIndex) {
        if (frameIndex < 0 || frameIndex >= frameCount) {
            throw new IllegalArgumentException("frameIndex out of clip range");
        }
    }
}
