package umbra.boss;

public record BossAttackDefinition(
        String id,
        String phaseId,
        String clipId,
        String hitboxProfile,
        float minRange,
        float maxRange,
        float cooldownSeconds,
        int frameCount,
        float fps,
        int damage,
        float knockbackX,
        float knockbackY,
        float hitPauseSeconds,
        float hitStunSeconds,
        String damageType
) {
    public BossAttackDefinition {
        requireSnakeCase("attack id", id);
        requireSnakeCase("phaseId", phaseId);
        requireId("clipId", clipId);
        requireSnakeCase("hitboxProfile", hitboxProfile);
        if (minRange < 0.0f || maxRange < minRange) {
            throw new BossDefinitionValidationException("invalid attack range: " + id);
        }
        if (cooldownSeconds < 0.0f) {
            throw new BossDefinitionValidationException("cooldownSeconds must not be negative: " + id);
        }
        if (frameCount <= 0) {
            throw new BossDefinitionValidationException("frameCount must be positive: " + id);
        }
        if (fps <= 0.0f) {
            throw new BossDefinitionValidationException("fps must be positive: " + id);
        }
        if (damage <= 0) {
            throw new BossDefinitionValidationException("damage must be positive: " + id);
        }
        if (hitPauseSeconds < 0.0f || hitStunSeconds < 0.0f) {
            throw new BossDefinitionValidationException("hit timing must not be negative: " + id);
        }
        if (damageType == null || damageType.isBlank()) {
            damageType = "physical";
        }
    }

    public BossAttackPattern pattern() {
        return new BossAttackPattern(id, phaseId, minRange, maxRange, cooldownSeconds);
    }

    private static void requireSnakeCase(String field, String value) {
        requireId(field, value);
        if (!value.matches("[a-z][a-z0-9_]*")) {
            throw new BossDefinitionValidationException(field + " must be snake_case: " + value);
        }
    }

    private static void requireId(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new BossDefinitionValidationException(field + " must not be blank");
        }
    }
}
