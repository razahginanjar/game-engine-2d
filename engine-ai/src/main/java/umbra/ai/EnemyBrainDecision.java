package umbra.ai;

public record EnemyBrainDecision(
        EnemyAiState state,
        int desiredDirection,
        boolean wantsAttack,
        boolean wantsEvade
) {
    public EnemyBrainDecision {
        if (desiredDirection < -1 || desiredDirection > 1) {
            throw new IllegalArgumentException("desiredDirection must be -1, 0, or 1");
        }
    }
}
