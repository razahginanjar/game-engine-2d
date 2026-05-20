package umbra.boss;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BossAttackSelectorTest {
    @Test
    void selectsMatchingAttackForPhaseAndRange() {
        BossAttackSelector selector = new BossAttackSelector();

        Optional<BossAttackPattern> selected = selector.update("phase_1", 96.0f, patterns(), 0.016f);

        assertTrue(selected.isPresent());
        assertEquals("stab", selected.get().id());
    }

    @Test
    void respectsCooldown() {
        BossAttackSelector selector = new BossAttackSelector();
        selector.update("phase_1", 96.0f, patterns(), 0.016f);

        Optional<BossAttackPattern> selected = selector.update("phase_1", 96.0f, patterns(), 0.1f);

        assertTrue(selected.isEmpty());
    }

    @Test
    void usesPhaseSpecificAttack() {
        BossAttackSelector selector = new BossAttackSelector();

        Optional<BossAttackPattern> selected = selector.update("phase_2", 96.0f, patterns(), 0.016f);

        assertTrue(selected.isPresent());
        assertEquals("sweep", selected.get().id());
    }

    private List<BossAttackPattern> patterns() {
        return List.of(
                new BossAttackPattern("stab", "phase_1", 32.0f, 160.0f, 0.5f),
                new BossAttackPattern("sweep", "phase_2", 32.0f, 192.0f, 0.4f)
        );
    }
}
