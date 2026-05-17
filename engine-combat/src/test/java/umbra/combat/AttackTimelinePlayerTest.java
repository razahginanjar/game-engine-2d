package umbra.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AttackTimelinePlayerTest {
    @Test
    void attackProgressesThroughStartupActiveRecoveryFinished() {
        AttackTimelinePlayer player = new AttackTimelinePlayer();
        player.start(timeline(0.10f, 0.20f, 0.30f));

        assertEquals(AttackPhase.STARTUP, player.phase());
        assertFalse(player.hitboxActive());

        player.update(0.10f);
        assertEquals(AttackPhase.ACTIVE, player.phase());
        assertTrue(player.hitboxActive());

        player.update(0.20f);
        assertEquals(AttackPhase.RECOVERY, player.phase());
        assertFalse(player.hitboxActive());

        player.update(0.30f);
        assertEquals(AttackPhase.FINISHED, player.phase());
        assertTrue(player.acceptingNewAttack());
    }

    @Test
    void zeroStartupBeginsActiveImmediately() {
        AttackTimelinePlayer player = new AttackTimelinePlayer();

        player.start(timeline(0.0f, 0.20f, 0.10f));

        assertEquals(AttackPhase.ACTIVE, player.phase());
        assertTrue(player.hitboxActive());
    }

    @Test
    void finishedAttackIgnoresFurtherTime() {
        AttackTimelinePlayer player = new AttackTimelinePlayer();
        player.start(timeline(0.0f, 0.10f, 0.0f));
        player.update(0.10f);

        player.update(10.0f);

        assertEquals(AttackPhase.FINISHED, player.phase());
    }

    @Test
    void rejectsNegativeDelta() {
        AttackTimelinePlayer player = new AttackTimelinePlayer();
        player.start(timeline(0.0f, 0.10f, 0.0f));

        assertThrows(IllegalArgumentException.class, () -> player.update(-0.01f));
    }

    private AttackTimelineDefinition timeline(float startup, float active, float recovery) {
        return new AttackTimelineDefinition(
                new AttackDefinition("player_slash_01", 1, 160.0f, 40.0f, 0.045f, "slash"),
                startup,
                active,
                recovery
        );
    }
}
