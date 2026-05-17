package umbra.combat;

/**
 * Timing definition for an attack. Hitboxes are only active during ACTIVE.
 */
public record AttackTimelineDefinition(
        AttackDefinition attack,
        float startupSeconds,
        float activeSeconds,
        float recoverySeconds
) {
    public AttackTimelineDefinition {
        if (attack == null) {
            throw new IllegalArgumentException("attack must not be null");
        }
        if (startupSeconds < 0.0f || activeSeconds <= 0.0f || recoverySeconds < 0.0f) {
            throw new IllegalArgumentException("invalid attack timing");
        }
    }

    public float totalSeconds() {
        return startupSeconds + activeSeconds + recoverySeconds;
    }
}
