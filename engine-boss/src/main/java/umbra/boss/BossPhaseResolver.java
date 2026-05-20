package umbra.boss;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class BossPhaseResolver {
    private final List<BossPhaseDefinition> phases;

    public BossPhaseResolver(List<BossPhaseDefinition> phases) {
        if (phases == null || phases.isEmpty()) {
            throw new IllegalArgumentException("phases must not be empty");
        }
        this.phases = phases.stream()
                .sorted(Comparator.comparing(BossPhaseDefinition::startsAtHealthRatio).reversed())
                .toList();
    }

    public BossPhaseDefinition resolve(int currentHealth, int maxHealth) {
        if (maxHealth <= 0) {
            throw new IllegalArgumentException("maxHealth must be positive");
        }
        float ratio = Math.max(0.0f, Math.min(1.0f, currentHealth / (float) maxHealth));
        BossPhaseDefinition selected = phases.get(0);
        for (BossPhaseDefinition phase : phases) {
            if (ratio <= phase.startsAtHealthRatio()) {
                selected = phase;
            }
        }
        return Objects.requireNonNull(selected);
    }
}
