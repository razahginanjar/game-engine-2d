package umbra.physics.enemy;

/**
 * Movement tuning for simple ground patrol enemies.
 */
public record PatrolControllerConfig(
        float speedPixelsPerSecond,
        float gravityPixelsPerSecondSquared,
        float maxFallSpeedPixelsPerSecond
) {
    public PatrolControllerConfig {
        if (speedPixelsPerSecond <= 0.0f) {
            throw new IllegalArgumentException("speedPixelsPerSecond must be positive");
        }
        if (gravityPixelsPerSecondSquared <= 0.0f) {
            throw new IllegalArgumentException("gravityPixelsPerSecondSquared must be positive");
        }
        if (maxFallSpeedPixelsPerSecond <= 0.0f) {
            throw new IllegalArgumentException("maxFallSpeedPixelsPerSecond must be positive");
        }
    }

    public static PatrolControllerConfig slimeDefaults() {
        return new PatrolControllerConfig(55.0f, 1200.0f, 420.0f);
    }
}
