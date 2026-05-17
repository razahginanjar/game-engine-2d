package umbra.core;

/**
 * Drives deterministic scene updates with a fixed timestep while allowing
 * render calls to happen at the platform frame rate.
 */
public final class FixedStepEngine {
    private final EngineConfig config;
    private final SceneManager sceneManager;
    private float accumulatorSeconds;
    private float maxFrameDeltaSeconds = 0.25f;

    public FixedStepEngine(EngineConfig config, SceneManager sceneManager) {
        this.config = config;
        this.sceneManager = sceneManager;
    }

    public void update(float frameDeltaSeconds) {
        float clampedDelta = Math.min(Math.max(frameDeltaSeconds, 0.0f), maxFrameDeltaSeconds);
        accumulatorSeconds += clampedDelta;

        float fixedDelta = config.fixedDeltaSeconds();
        while (accumulatorSeconds >= fixedDelta) {
            sceneManager.update(fixedDelta);
            accumulatorSeconds -= fixedDelta;
        }
    }

    public void render() {
        sceneManager.render();
    }

    public float interpolationAlpha() {
        return accumulatorSeconds / config.fixedDeltaSeconds();
    }

    public void setMaxFrameDeltaSeconds(float maxFrameDeltaSeconds) {
        if (maxFrameDeltaSeconds <= 0.0f) {
            throw new IllegalArgumentException("maxFrameDeltaSeconds must be positive");
        }
        this.maxFrameDeltaSeconds = maxFrameDeltaSeconds;
    }
}
