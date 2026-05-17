package umbra.combat;

/**
 * Immutable event emitted when a hitbox damages a hurtbox.
 */
public record DamageEvent(
        int sourceEntityId,
        int targetEntityId,
        String attackId,
        int damage,
        float knockbackX,
        float knockbackY,
        float hitPauseSeconds,
        String damageType
) {
}
