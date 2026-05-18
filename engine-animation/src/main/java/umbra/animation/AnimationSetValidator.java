package umbra.animation;

import java.util.List;
import java.util.Objects;

public final class AnimationSetValidator {
    public void requireClips(AnimationSetDefinition animationSet, List<String> requiredClipIds) {
        Objects.requireNonNull(animationSet, "animationSet must not be null");
        Objects.requireNonNull(requiredClipIds, "requiredClipIds must not be null");
        for (String clipId : requiredClipIds) {
            if (clipId == null || clipId.isBlank()) {
                throw new AnimationValidationException("required clip id must not be blank");
            }
            if (!animationSet.hasClip(clipId)) {
                throw new AnimationValidationException(
                        "animation set " + animationSet.id() + " is missing required clip: " + clipId
                );
            }
        }
    }
}
