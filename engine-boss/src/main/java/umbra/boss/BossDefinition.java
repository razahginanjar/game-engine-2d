package umbra.boss;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record BossDefinition(
        String id,
        String displayName,
        int maxHealth,
        List<BossPhaseDefinition> phases,
        List<BossAttackDefinition> attacks
) {
    public BossDefinition {
        requireSnakeCase("boss id", id);
        if (displayName == null || displayName.isBlank()) {
            throw new BossDefinitionValidationException("displayName must not be blank");
        }
        if (maxHealth <= 0) {
            throw new BossDefinitionValidationException("maxHealth must be positive");
        }
        phases = phases == null ? List.of() : List.copyOf(phases);
        attacks = attacks == null ? List.of() : List.copyOf(attacks);
        if (phases.isEmpty()) {
            throw new BossDefinitionValidationException("boss must define at least one phase");
        }
        if (attacks.isEmpty()) {
            throw new BossDefinitionValidationException("boss must define at least one attack");
        }
        validateUniquePhases(phases);
        validateUniqueAttacks(attacks);
        validateAttackPhases(phases, attacks);
    }

    public BossAttackDefinition attack(String attackId) {
        for (BossAttackDefinition attack : attacks) {
            if (attack.id().equals(attackId)) {
                return attack;
            }
        }
        throw new BossDefinitionValidationException("unknown boss attack: " + attackId);
    }

    public List<BossAttackPattern> attackPatterns() {
        return attacks.stream()
                .map(BossAttackDefinition::pattern)
                .toList();
    }

    private static void validateUniquePhases(List<BossPhaseDefinition> phases) {
        Set<String> ids = new HashSet<>();
        for (BossPhaseDefinition phase : phases) {
            Objects.requireNonNull(phase, "phase must not be null");
            requireSnakeCase("phase id", phase.id());
            if (!ids.add(phase.id())) {
                throw new BossDefinitionValidationException("duplicate phase id: " + phase.id());
            }
        }
    }

    private static void validateUniqueAttacks(List<BossAttackDefinition> attacks) {
        Set<String> ids = new HashSet<>();
        for (BossAttackDefinition attack : attacks) {
            Objects.requireNonNull(attack, "attack must not be null");
            if (!ids.add(attack.id())) {
                throw new BossDefinitionValidationException("duplicate attack id: " + attack.id());
            }
        }
    }

    private static void validateAttackPhases(List<BossPhaseDefinition> phases, List<BossAttackDefinition> attacks) {
        Set<String> phaseIds = new HashSet<>();
        for (BossPhaseDefinition phase : phases) {
            phaseIds.add(phase.id());
        }
        for (BossAttackDefinition attack : attacks) {
            if (!phaseIds.contains(attack.phaseId())) {
                throw new BossDefinitionValidationException("attack references missing phase: " + attack.id());
            }
        }
    }

    private static void requireSnakeCase(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new BossDefinitionValidationException(field + " must not be blank");
        }
        if (!value.matches("[a-z][a-z0-9_]*")) {
            throw new BossDefinitionValidationException(field + " must be snake_case: " + value);
        }
    }
}
