package umbra.ai;

import java.util.Objects;

/**
 * Deterministic enemy state machine for sight, approach, attack preparation,
 * and reactive evasion.
 */
public final class EnemyBrain {
    private final EnemyBrainConfig config;
    private int randomState;
    private EnemyAiState state = EnemyAiState.PATROL;
    private float cautiousSeconds;
    private float attackSeconds;
    private float evadeSeconds;
    private int evadeDirection = -1;

    public EnemyBrain(EnemyBrainConfig config, int seed) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.randomState = seed == 0 ? 0x13579BDF : seed;
    }

    public EnemyBrainDecision update(EnemyBrainInput input) {
        Objects.requireNonNull(input, "input must not be null");
        if (input.defeated()) {
            state = EnemyAiState.DEAD;
            return new EnemyBrainDecision(state, 0, false, false);
        }
        if (input.hitStunned()) {
            state = EnemyAiState.HIT_STUN;
            cautiousSeconds = 0.0f;
            attackSeconds = 0.0f;
            evadeSeconds = 0.0f;
            return new EnemyBrainDecision(state, 0, false, false);
        }

        if (attackSeconds > 0.0f) {
            attackSeconds = Math.max(0.0f, attackSeconds - input.deltaSeconds());
            state = EnemyAiState.ATTACK;
            return new EnemyBrainDecision(state, directionToPlayer(input), true, false);
        }
        if (evadeSeconds > 0.0f) {
            evadeSeconds = Math.max(0.0f, evadeSeconds - input.deltaSeconds());
            state = EnemyAiState.EVADE;
            return new EnemyBrainDecision(state, evadeDirection, false, true);
        }

        float dx = input.playerCenterX() - input.enemyCenterX();
        float dy = input.playerCenterY() - input.enemyCenterY();
        float absDx = Math.abs(dx);
        boolean seesPlayer = absDx <= config.visionRangeX() && Math.abs(dy) <= config.visionRangeY();
        if (!seesPlayer) {
            cautiousSeconds = 0.0f;
            state = EnemyAiState.PATROL;
            return new EnemyBrainDecision(state, 0, false, false);
        }

        int directionToPlayer = directionToPlayer(input);
        if (input.playerAttackActive() && absDx <= config.evadeThreatRange() && rollEvade()) {
            evadeDirection = -directionToPlayer;
            evadeSeconds = config.evadeDurationSeconds();
            cautiousSeconds = 0.0f;
            state = EnemyAiState.EVADE;
            return new EnemyBrainDecision(state, evadeDirection, false, true);
        }

        if (absDx <= config.cautiousDistance()) {
            cautiousSeconds += input.deltaSeconds();
            if (cautiousSeconds >= config.attackWindupSeconds() && absDx <= config.attackRange()) {
                startAttack(directionToPlayer);
                return new EnemyBrainDecision(state, directionToPlayer, true, false);
            }
            state = EnemyAiState.CAUTIOUS;
            return new EnemyBrainDecision(state, directionToPlayer, false, false);
        }

        cautiousSeconds = 0.0f;
        state = EnemyAiState.CHASE;
        return new EnemyBrainDecision(state, directionToPlayer, false, false);
    }

    public EnemyAiState state() {
        return state;
    }

    public void reset() {
        state = EnemyAiState.PATROL;
        cautiousSeconds = 0.0f;
        attackSeconds = 0.0f;
        evadeSeconds = 0.0f;
        evadeDirection = -1;
    }

    private void startAttack(int directionToPlayer) {
        cautiousSeconds = 0.0f;
        attackSeconds = config.attackDurationSeconds();
        state = EnemyAiState.ATTACK;
    }

    private int directionToPlayer(EnemyBrainInput input) {
        return input.playerCenterX() >= input.enemyCenterX() ? 1 : -1;
    }

    private boolean rollEvade() {
        randomState = randomState * 1103515245 + 12345;
        int positive = randomState & 0x7fffffff;
        float value = positive / (float) Integer.MAX_VALUE;
        return value < config.evadeChance();
    }
}
