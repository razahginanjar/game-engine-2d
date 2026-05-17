package umbra.combat;

/**
 * Static attack tuning. Timing activation is handled by animation or combat
 * timeline systems; this definition only describes the damage payload.
 */
public record AttackDefinition(
        String id,
        int damage,
        float knockbackX,
        float knockbackY,
        float hitPauseSeconds,
        float hitStunSeconds,
        String damageType
) {
    public AttackDefinition(String id, int damage, float knockbackX, float knockbackY, float hitPauseSeconds, String damageType) {
        this(id, damage, knockbackX, knockbackY, hitPauseSeconds, 0.0f, damageType);
    }

    public AttackDefinition {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("attack id must not be blank");
        }
        if (damage <= 0) {
            throw new IllegalArgumentException("damage must be positive");
        }
        if (hitPauseSeconds < 0.0f) {
            throw new IllegalArgumentException("hitPauseSeconds must not be negative");
        }
        if (hitStunSeconds < 0.0f) {
            throw new IllegalArgumentException("hitStunSeconds must not be negative");
        }
        if (damageType == null || damageType.isBlank()) {
            damageType = "physical";
        }
    }
}
