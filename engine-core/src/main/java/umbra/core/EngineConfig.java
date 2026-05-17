package umbra.core;

/**
 * Immutable runtime configuration shared by engine modules.
 *
 * @param gameTitle displayed title for desktop launchers and diagnostics
 * @param viewportWidth internal render width in pixels
 * @param viewportHeight internal render height in pixels
 * @param tileSize collision tile size in pixels
 * @param fixedTimestepHz deterministic update rate
 */
public record EngineConfig(
        String gameTitle,
        int viewportWidth,
        int viewportHeight,
        int tileSize,
        int fixedTimestepHz
) {
    public EngineConfig {
        if (gameTitle == null || gameTitle.isBlank()) {
            throw new IllegalArgumentException("gameTitle must not be blank");
        }
        if (viewportWidth <= 0 || viewportHeight <= 0) {
            throw new IllegalArgumentException("viewport dimensions must be positive");
        }
        if (tileSize <= 0) {
            throw new IllegalArgumentException("tileSize must be positive");
        }
        if (fixedTimestepHz <= 0) {
            throw new IllegalArgumentException("fixedTimestepHz must be positive");
        }
    }

    public static EngineConfig metroidvaniaDefaults() {
        return new EngineConfig("Umbra2D Sample", 1280, 720, 32, 60);
    }

    public float fixedDeltaSeconds() {
        return 1.0f / fixedTimestepHz;
    }
}
