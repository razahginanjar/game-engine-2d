package umbra.ai;

public record EnemyBrainInput(
        float enemyCenterX,
        float enemyCenterY,
        float playerCenterX,
        float playerCenterY,
        boolean playerAttackActive,
        boolean hitStunned,
        boolean defeated,
        float deltaSeconds
) {
    public EnemyBrainInput {
        if (deltaSeconds < 0.0f) {
            throw new IllegalArgumentException("deltaSeconds must not be negative");
        }
    }
}
