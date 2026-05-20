package umbra.project;

public record CreatureCombatProfile(
        int maxHealth,
        int contactDamage,
        int attackDamage,
        String attackActiveEvent
) {
    public CreatureCombatProfile {
        if (maxHealth <= 0) {
            throw new CreatureDefinitionValidationException("combat.max_health must be positive");
        }
        if (contactDamage < 0) {
            throw new CreatureDefinitionValidationException("combat.contact_damage must not be negative");
        }
        if (attackDamage < 0) {
            throw new CreatureDefinitionValidationException("combat.attack_damage must not be negative");
        }
        if (attackDamage > 0 && (attackActiveEvent == null || attackActiveEvent.isBlank())) {
            throw new CreatureDefinitionValidationException("combat.attack_active_event is required when attack_damage is positive");
        }
        if (attackActiveEvent == null) {
            attackActiveEvent = "";
        }
    }
}
