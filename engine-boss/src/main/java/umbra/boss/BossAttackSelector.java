package umbra.boss;

import java.util.List;
import java.util.Optional;

public final class BossAttackSelector {
    private float cooldownSeconds;
    private int cursor;

    public Optional<BossAttackPattern> update(
            String phaseId,
            float targetDistance,
            List<BossAttackPattern> patterns,
            float deltaSeconds
    ) {
        if (phaseId == null || phaseId.isBlank()) {
            throw new IllegalArgumentException("phaseId must not be blank");
        }
        if (targetDistance < 0.0f) {
            throw new IllegalArgumentException("targetDistance must not be negative");
        }
        if (deltaSeconds < 0.0f) {
            throw new IllegalArgumentException("deltaSeconds must not be negative");
        }
        if (patterns == null || patterns.isEmpty()) {
            return Optional.empty();
        }
        cooldownSeconds = Math.max(0.0f, cooldownSeconds - deltaSeconds);
        if (cooldownSeconds > 0.0f) {
            return Optional.empty();
        }
        for (int attempt = 0; attempt < patterns.size(); attempt++) {
            BossAttackPattern pattern = patterns.get((cursor + attempt) % patterns.size());
            if (pattern.phaseId().equals(phaseId)
                    && targetDistance >= pattern.minRange()
                    && targetDistance <= pattern.maxRange()) {
                cursor = (cursor + attempt + 1) % patterns.size();
                cooldownSeconds = pattern.cooldownSeconds();
                return Optional.of(pattern);
            }
        }
        return Optional.empty();
    }

    public void reset() {
        cooldownSeconds = 0.0f;
        cursor = 0;
    }
}
