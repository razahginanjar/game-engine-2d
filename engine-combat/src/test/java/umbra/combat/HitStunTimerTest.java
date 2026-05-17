package umbra.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HitStunTimerTest {
    @Test
    void triggerStartsStunAndUpdateExpiresIt() {
        HitStunTimer timer = new HitStunTimer();

        timer.trigger(0.20f);
        timer.update(0.05f);

        assertTrue(timer.stunned());
        assertEquals(0.15f, timer.remainingSeconds(), 0.0001f);

        timer.update(0.15f);

        assertFalse(timer.stunned());
    }

    @Test
    void triggerKeepsLongerExistingStun() {
        HitStunTimer timer = new HitStunTimer();

        timer.trigger(0.30f);
        timer.trigger(0.05f);

        assertEquals(0.30f, timer.remainingSeconds(), 0.0001f);
    }

    @Test
    void resetClearsStun() {
        HitStunTimer timer = new HitStunTimer();

        timer.trigger(0.20f);
        timer.reset();

        assertFalse(timer.stunned());
    }

    @Test
    void rejectsNegativeTimes() {
        HitStunTimer timer = new HitStunTimer();

        assertThrows(IllegalArgumentException.class, () -> timer.trigger(-0.01f));
        assertThrows(IllegalArgumentException.class, () -> timer.update(-0.01f));
    }
}
