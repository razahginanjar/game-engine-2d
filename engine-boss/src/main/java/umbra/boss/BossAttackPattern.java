package umbra.boss;

public record BossAttackPattern(
        String id,
        String phaseId,
        float minRange,
        float maxRange,
        float cooldownSeconds
) {
    public BossAttackPattern {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("attack id must not be blank");
        }
        if (phaseId == null || phaseId.isBlank()) {
            throw new IllegalArgumentException("phaseId must not be blank");
        }
        if (minRange < 0.0f || maxRange < minRange) {
            throw new IllegalArgumentException("invalid range");
        }
        if (cooldownSeconds < 0.0f) {
            throw new IllegalArgumentException("cooldownSeconds must not be negative");
        }
    }
}
