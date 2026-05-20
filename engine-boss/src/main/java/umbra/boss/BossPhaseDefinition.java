package umbra.boss;

/**
 * Health threshold for a boss phase. A phase with threshold 0.5 starts when
 * current health ratio is less than or equal to 50%.
 */
public record BossPhaseDefinition(String id, float startsAtHealthRatio) {
    public BossPhaseDefinition {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("phase id must not be blank");
        }
        if (startsAtHealthRatio < 0.0f || startsAtHealthRatio > 1.0f) {
            throw new IllegalArgumentException("startsAtHealthRatio must be between 0 and 1");
        }
    }
}
