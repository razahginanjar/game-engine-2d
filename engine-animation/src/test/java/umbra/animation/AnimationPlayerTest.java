package umbra.animation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnimationPlayerTest {
    @Test
    void loopsFramesDeterministically() {
        AnimationClipDefinition clip = new AnimationClipDefinition(
                "idle",
                "idle_sheet",
                List.of(),
                0,
                0,
                32,
                32,
                4,
                10.0f,
                true,
                List.of()
        );
        AnimationPlayer player = new AnimationPlayer();

        player.play(clip);
        player.update(0.35f);

        assertEquals(3, player.frameIndex());

        player.update(0.10f);

        assertEquals(0, player.frameIndex());
        assertFalse(player.finished());
    }

    @Test
    void nonLoopingClipStopsOnLastFrame() {
        AnimationClipDefinition clip = new AnimationClipDefinition(
                "hit",
                "hit_sheet",
                List.of(),
                0,
                0,
                32,
                32,
                3,
                10.0f,
                false,
                List.of()
        );
        AnimationPlayer player = new AnimationPlayer();

        player.play(clip);
        player.update(0.5f);
        player.update(0.5f);

        assertEquals(2, player.frameIndex());
        assertTrue(player.finished());
    }
}
