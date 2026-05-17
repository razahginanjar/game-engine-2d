package umbra.animation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnimationPlayerTest {
    @Test
    void loopedClipWrapsFrameIndex() {
        AnimationPlayer player = new AnimationPlayer();
        player.play(new AnimationClipDefinition("run", "run.png", 4, 10, true));

        player.update(0.45f);

        assertEquals(0, player.frameIndex());
        assertFalse(player.finished());
    }

    @Test
    void nonLoopedClipStopsOnLastFrame() {
        AnimationPlayer player = new AnimationPlayer();
        player.play(new AnimationClipDefinition("attack", "attack.png", 4, 10, false));

        player.update(1.0f);

        assertEquals(3, player.frameIndex());
        assertTrue(player.finished());
    }

    @Test
    void sameClipDoesNotRestartImplicitly() {
        AnimationClipDefinition idle = new AnimationClipDefinition("idle", "idle.png", 4, 10, true);
        AnimationPlayer player = new AnimationPlayer();
        player.play(idle);
        player.update(0.2f);

        player.play(idle);

        assertEquals(2, player.frameIndex());
    }

    @Test
    void rejectsNegativeDelta() {
        AnimationPlayer player = new AnimationPlayer();
        player.play(new AnimationClipDefinition("idle", "idle.png", 1, 10, true));

        assertThrows(IllegalArgumentException.class, () -> player.update(-0.1f));
    }
}
