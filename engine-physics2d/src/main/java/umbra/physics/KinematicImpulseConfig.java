package umbra.physics;

/**
 * Gravity limits for externally-driven kinematic impulses such as knockback.
 */
public record KinematicImpulseConfig(
        float gravityPixelsPerSecondSquared,
        float maxFallSpeedPixelsPerSecond
) {
    public KinematicImpulseConfig {
        if (gravityPixelsPerSecondSquared <= 0.0f) {
            throw new IllegalArgumentException("gravityPixelsPerSecondSquared must be positive");
        }
        if (maxFallSpeedPixelsPerSecond <= 0.0f) {
            throw new IllegalArgumentException("maxFallSpeedPixelsPerSecond must be positive");
        }
    }
}
