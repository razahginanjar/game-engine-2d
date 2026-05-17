package umbra.combat;

/**
 * Deterministic attack timeline state machine. Gameplay starts an attack, then
 * systems query phase to enable hitboxes only during active windows.
 */
public final class AttackTimelinePlayer {
    private AttackTimelineDefinition timeline;
    private float elapsedSeconds;
    private AttackPhase phase = AttackPhase.IDLE;

    public void start(AttackTimelineDefinition timeline) {
        this.timeline = timeline;
        this.elapsedSeconds = 0.0f;
        this.phase = timeline.startupSeconds() > 0.0f ? AttackPhase.STARTUP : AttackPhase.ACTIVE;
    }

    public void reset() {
        timeline = null;
        elapsedSeconds = 0.0f;
        phase = AttackPhase.IDLE;
    }

    public void update(float deltaSeconds) {
        if (deltaSeconds < 0.0f) {
            throw new IllegalArgumentException("deltaSeconds must not be negative");
        }
        if (timeline == null || phase == AttackPhase.IDLE || phase == AttackPhase.FINISHED) {
            return;
        }

        elapsedSeconds += deltaSeconds;
        if (elapsedSeconds < timeline.startupSeconds()) {
            phase = AttackPhase.STARTUP;
        } else if (elapsedSeconds < timeline.startupSeconds() + timeline.activeSeconds()) {
            phase = AttackPhase.ACTIVE;
        } else if (elapsedSeconds < timeline.totalSeconds()) {
            phase = AttackPhase.RECOVERY;
        } else {
            phase = AttackPhase.FINISHED;
        }
    }

    public AttackTimelineDefinition timeline() {
        return timeline;
    }

    public AttackPhase phase() {
        return phase;
    }

    public boolean hitboxActive() {
        return phase == AttackPhase.ACTIVE;
    }

    public boolean acceptingNewAttack() {
        return phase == AttackPhase.IDLE || phase == AttackPhase.FINISHED;
    }

    public float elapsedSeconds() {
        return elapsedSeconds;
    }
}
