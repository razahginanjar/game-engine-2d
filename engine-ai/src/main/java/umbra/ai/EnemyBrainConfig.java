package umbra.ai;

public record EnemyBrainConfig(
        float visionRangeX,
        float visionRangeY,
        float cautiousDistance,
        float attackRange,
        float attackWindupSeconds,
        float attackDurationSeconds,
        float evadeThreatRange,
        float evadeChance,
        float evadeDurationSeconds
) {
    public EnemyBrainConfig {
        if (visionRangeX <= 0.0f || visionRangeY <= 0.0f) {
            throw new IllegalArgumentException("vision range must be positive");
        }
        if (cautiousDistance <= 0.0f || attackRange <= 0.0f) {
            throw new IllegalArgumentException("engagement ranges must be positive");
        }
        if (attackRange > cautiousDistance) {
            throw new IllegalArgumentException("attackRange must not exceed cautiousDistance");
        }
        if (attackWindupSeconds < 0.0f || attackDurationSeconds <= 0.0f || evadeDurationSeconds <= 0.0f) {
            throw new IllegalArgumentException("timings must be valid");
        }
        if (evadeThreatRange <= 0.0f || evadeChance < 0.0f || evadeChance > 1.0f) {
            throw new IllegalArgumentException("evade settings must be valid");
        }
    }

    public static EnemyBrainConfig standardMelee() {
        return new EnemyBrainConfig(
                260.0f,
                96.0f,
                32.0f,
                24.0f,
                0.28f,
                0.62f,
                56.0f,
                0.45f,
                0.22f
        );
    }

    public static EnemyBrainConfig flyingMelee() {
        return new EnemyBrainConfig(
                300.0f,
                160.0f,
                42.0f,
                30.0f,
                0.20f,
                0.62f,
                70.0f,
                0.55f,
                0.24f
        );
    }
}
