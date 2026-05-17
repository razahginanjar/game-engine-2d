package umbra.physics.player;

/**
 * Tunable player movement constants. Units are pixels and seconds.
 */
public record PlayerControllerConfig(
        float moveMaxSpeedPixelsPerSecond,
        float groundAccelerationPixelsPerSecondSquared,
        float groundDecelerationPixelsPerSecondSquared,
        float airAccelerationPixelsPerSecondSquared,
        float gravityPixelsPerSecondSquared,
        float maxFallSpeedPixelsPerSecond,
        float jumpSpeedPixelsPerSecond,
        float jumpCutMultiplier,
        float coyoteTimeSeconds,
        float jumpBufferSeconds
) {
    public PlayerControllerConfig {
        if (moveMaxSpeedPixelsPerSecond <= 0.0f
                || groundAccelerationPixelsPerSecondSquared <= 0.0f
                || groundDecelerationPixelsPerSecondSquared <= 0.0f
                || airAccelerationPixelsPerSecondSquared <= 0.0f
                || gravityPixelsPerSecondSquared <= 0.0f
                || maxFallSpeedPixelsPerSecond <= 0.0f
                || jumpSpeedPixelsPerSecond <= 0.0f
                || jumpCutMultiplier <= 0.0f
                || jumpCutMultiplier > 1.0f
                || coyoteTimeSeconds < 0.0f
                || jumpBufferSeconds < 0.0f) {
            throw new IllegalArgumentException("invalid player controller constants");
        }
    }

    public static PlayerControllerConfig metroidvaniaDefaults() {
        return new PlayerControllerConfig(
                165.0f,
                1800.0f,
                2200.0f,
                1100.0f,
                1450.0f,
                480.0f,
                430.0f,
                0.45f,
                0.10f,
                0.12f
        );
    }
}
