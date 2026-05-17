package umbra.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HitPauseTimerTest {
    @Test
    void triggerStartsPauseAndUpdateExpiresIt() {
        HitPauseTimer timer = new HitPauseTimer();

        timer.trigger(0.10f);
        timer.update(0.04f);

        assertTrue(timer.paused());
        assertEquals(0.06f, timer.remainingSeconds(), 0.0001f);

        timer.update(0.06f);

        assertFalse(timer.paused());
        assertEquals(0.0f, timer.remainingSeconds(), 0.0001f);
    }

    @Test
    void triggerKeepsLongerExistingPause() {
        HitPauseTimer timer = new HitPauseTimer();

        timer.trigger(0.20f);
        timer.trigger(0.05f);

        assertEquals(0.20f, timer.remainingSeconds(), 0.0001f);
    }

    @Test
    void resetClearsPause() {
        HitPauseTimer timer = new HitPauseTimer();

        timer.trigger(0.20f);
        timer.reset();

        assertFalse(timer.paused());
    }

    @Test
    void rejectsNegativeTimes() {
        HitPauseTimer timer = new HitPauseTimer();

        assertThrows(IllegalArgumentException.class, () -> timer.trigger(-0.01f));
        assertThrows(IllegalArgumentException.class, () -> timer.update(-0.01f));
    }
}
