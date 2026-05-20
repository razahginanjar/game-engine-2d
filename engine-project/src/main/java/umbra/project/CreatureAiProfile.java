package umbra.project;

public record CreatureAiProfile(
        String profile,
        float visionRange,
        float cautionRange,
        float attackRange,
        float evadeProbability,
        boolean usesShield
) {
    public CreatureAiProfile {
        requireText("ai.profile", profile);
        requirePositive("ai.vision_range", visionRange);
        requirePositive("ai.caution_range", cautionRange);
        requirePositive("ai.attack_range", attackRange);
        if (cautionRange > visionRange) {
            throw new CreatureDefinitionValidationException("ai.caution_range must not exceed ai.vision_range");
        }
        if (attackRange > cautionRange) {
            throw new CreatureDefinitionValidationException("ai.attack_range must not exceed ai.caution_range");
        }
        if (evadeProbability < 0.0f || evadeProbability > 1.0f) {
            throw new CreatureDefinitionValidationException("ai.evade_probability must be between 0 and 1");
        }
    }

    private static void requireText(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new CreatureDefinitionValidationException(field + " must not be blank");
        }
    }

    private static void requirePositive(String field, float value) {
        if (value <= 0.0f) {
            throw new CreatureDefinitionValidationException(field + " must be positive");
        }
    }
}
