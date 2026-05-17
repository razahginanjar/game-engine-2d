package umbra.animation;

import java.util.List;
import java.util.Optional;

/**
 * Metadata contract for a character or VFX animation set.
 */
public record AnimationSetDefinition(
        String assetId,
        int frameWidth,
        int frameHeight,
        String pivot,
        RectDefinition collisionBox,
        RectDefinition hurtbox,
        List<AnimationClipDefinition> clips
) {
    public AnimationSetDefinition {
        if (assetId == null || assetId.isBlank()) {
            throw new AnimationValidationException("assetId must not be blank");
        }
        if (frameWidth <= 0 || frameHeight <= 0) {
            throw new AnimationValidationException("frame size must be positive");
        }
        if (pivot == null || pivot.isBlank()) {
            throw new AnimationValidationException("pivot must not be blank");
        }
        clips = List.copyOf(clips);
        if (clips.isEmpty()) {
            throw new AnimationValidationException("animation set must define at least one clip");
        }
    }

    public Optional<AnimationClipDefinition> clip(String id) {
        return clips.stream().filter(clip -> clip.id().equals(id)).findFirst();
    }
}
