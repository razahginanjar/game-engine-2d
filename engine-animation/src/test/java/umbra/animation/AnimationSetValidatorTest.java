package umbra.animation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class AnimationSetValidatorTest {
    @Test
    void acceptsAnimationSetWithRequiredClips() {
        AnimationSetDefinition set = new AnimationSetDefinition("player", List.of(clip("idle"), clip("run")));

        assertDoesNotThrow(() -> new AnimationSetValidator().requireClips(set, List.of("idle", "run")));
    }

    @Test
    void rejectsAnimationSetMissingRequiredClip() {
        AnimationSetDefinition set = new AnimationSetDefinition("player", List.of(clip("idle")));

        assertThrows(AnimationValidationException.class,
                () -> new AnimationSetValidator().requireClips(set, List.of("idle", "run")));
    }

    private AnimationClipDefinition clip(String id) {
        return new AnimationClipDefinition(
                id,
                id + "_texture",
                List.of(),
                0,
                0,
                32,
                32,
                1,
                12.0f,
                true,
                List.of()
        );
    }
}
