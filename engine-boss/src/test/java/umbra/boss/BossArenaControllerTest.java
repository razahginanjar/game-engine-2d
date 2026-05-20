package umbra.boss;

import org.junit.jupiter.api.Test;
import umbra.physics.Aabb;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BossArenaControllerTest {
    @Test
    void activatesAndLocksWhenPlayerEntersTrigger() {
        BossArenaController controller = controller();

        BossArenaStatus dormant = controller.update(input(new Aabb(32.0f, 32.0f, 16.0f, 32.0f), 10, false));
        BossArenaStatus active = controller.update(input(new Aabb(140.0f, 32.0f, 16.0f, 32.0f), 10, false));

        assertEquals(BossFightState.DORMANT, dormant.state());
        assertFalse(dormant.locked());
        assertEquals(BossFightState.ACTIVE, active.state());
        assertTrue(active.locked());
        assertEquals(List.of("left_exit"), active.lockedDoorIds());
    }

    @Test
    void unlocksWhenBossIsDefeated() {
        BossArenaController controller = controller();
        controller.update(input(new Aabb(140.0f, 32.0f, 16.0f, 32.0f), 10, false));

        BossArenaStatus defeated = controller.update(input(new Aabb(140.0f, 32.0f, 16.0f, 32.0f), 0, false));

        assertEquals(BossFightState.DEFEATED, defeated.state());
        assertFalse(defeated.locked());
    }

    @Test
    void persistedDefeatFlagStartsDefeated() {
        BossArenaStatus status = controller().update(input(new Aabb(140.0f, 32.0f, 16.0f, 32.0f), 10, true));

        assertEquals(BossFightState.DEFEATED, status.state());
        assertFalse(status.locked());
    }

    @Test
    void resolvesLowerHealthPhase() {
        BossArenaStatus status = controller().update(input(new Aabb(140.0f, 32.0f, 16.0f, 32.0f), 4, false));

        assertEquals("phase_2", status.phase().id());
    }

    private BossArenaController controller() {
        return new BossArenaController(
                new BossArenaDefinition(
                        "impaler_arena",
                        "impaler",
                        "boss_impaler_defeated",
                        new Aabb(64.0f, 0.0f, 320.0f, 160.0f),
                        new Aabb(128.0f, 0.0f, 96.0f, 160.0f),
                        List.of("left_exit")
                ),
                List.of(
                        new BossPhaseDefinition("phase_1", 1.0f),
                        new BossPhaseDefinition("phase_2", 0.5f)
                )
        );
    }

    private BossArenaInput input(Aabb playerBounds, int currentHealth, boolean defeatFlagSet) {
        return new BossArenaInput(playerBounds, currentHealth, 10, defeatFlagSet);
    }
}
