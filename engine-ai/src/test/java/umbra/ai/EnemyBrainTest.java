package umbra.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EnemyBrainTest {
    @Test
    void patrolsWhenPlayerIsOutsideVision() {
        EnemyBrain brain = new EnemyBrain(EnemyBrainConfig.standardMelee(), 7);

        EnemyBrainDecision decision = brain.update(input(0.0f, 0.0f, 500.0f, 0.0f, false, 0.016f));

        assertEquals(EnemyAiState.PATROL, decision.state());
        assertEquals(0, decision.desiredDirection());
    }

    @Test
    void chasesVisiblePlayerBeyondCautiousDistance() {
        EnemyBrain brain = new EnemyBrain(EnemyBrainConfig.standardMelee(), 7);

        EnemyBrainDecision decision = brain.update(input(100.0f, 0.0f, 220.0f, 0.0f, false, 0.016f));

        assertEquals(EnemyAiState.CHASE, decision.state());
        assertEquals(1, decision.desiredDirection());
    }

    @Test
    void becomesCautiousThenAttacksNearPlayer() {
        EnemyBrainConfig config = EnemyBrainConfig.standardMelee();
        EnemyBrain brain = new EnemyBrain(config, 7);

        EnemyBrainDecision first = brain.update(input(100.0f, 0.0f, 128.0f, 0.0f, false, 0.10f));
        EnemyBrainDecision second = brain.update(input(100.0f, 0.0f, 128.0f, 0.0f, false, config.attackWindupSeconds()));

        assertEquals(EnemyAiState.CAUTIOUS, first.state());
        assertFalse(first.wantsAttack());
        assertEquals(EnemyAiState.ATTACK, second.state());
        assertTrue(second.wantsAttack());
    }

    @Test
    void evadesPlayerAttackWhenThreatenedAndRollSucceeds() {
        EnemyBrainConfig config = new EnemyBrainConfig(260.0f, 96.0f, 32.0f, 24.0f, 0.2f, 0.3f, 80.0f, 1.0f, 0.2f);
        EnemyBrain brain = new EnemyBrain(config, 7);

        EnemyBrainDecision decision = brain.update(input(100.0f, 0.0f, 140.0f, 0.0f, true, 0.016f));

        assertEquals(EnemyAiState.EVADE, decision.state());
        assertEquals(-1, decision.desiredDirection());
        assertTrue(decision.wantsEvade());
    }

    private EnemyBrainInput input(float enemyX, float enemyY, float playerX, float playerY, boolean playerAttackActive, float deltaSeconds) {
        return new EnemyBrainInput(enemyX, enemyY, playerX, playerY, playerAttackActive, false, false, deltaSeconds);
    }
}
