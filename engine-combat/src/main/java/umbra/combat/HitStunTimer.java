package umbra.combat;

/**
 * Deterministic action-lock timer for hit reactions after receiving damage.
 */
public final class HitStunTimer {
    private static final float TIMER_EPSILON = 0.000001f;

    private float remainingSeconds;

    public void trigger(float durationSeconds) {
        if (durationSeconds < 0.0f) {
            throw new IllegalArgumentException("durationSeconds must not be negative");
        }
        remainingSeconds = Math.max(remainingSeconds, durationSeconds);
    }

    public void update(float deltaSeconds) {
        if (deltaSeconds < 0.0f) {
            throw new IllegalArgumentException("deltaSeconds must not be negative");
        }
        if (deltaSeconds + TIMER_EPSILON >= remainingSeconds) {
            remainingSeconds = 0.0f;
        } else {
            remainingSeconds -= deltaSeconds;
        }
    }

    public void reset() {
        remainingSeconds = 0.0f;
    }

    public boolean stunned() {
        return remainingSeconds > 0.0f;
    }

    public float remainingSeconds() {
        return remainingSeconds;
    }
}
