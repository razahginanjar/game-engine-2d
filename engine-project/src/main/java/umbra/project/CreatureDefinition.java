package umbra.project;

import java.util.List;

public record CreatureDefinition(
        String id,
        String displayName,
        String category,
        String assetRoot,
        String animationMetadata,
        List<String> tags,
        CreaturePhysicalSetup physical,
        CreatureAiProfile ai,
        CreatureStateModel states,
        CreatureCombatProfile combat
) {
    public CreatureDefinition {
        requireSnakeCase("id", id);
        requireText("display_name", displayName);
        requireSnakeCase("category", category);
        requireRelativePath("asset_root", assetRoot);
        requireRelativePath("animation_metadata", animationMetadata);
        tags = tags == null ? List.of() : List.copyOf(tags);
        for (String tag : tags) {
            requireSnakeCase("tag", tag);
        }
        if (physical == null) {
            throw new CreatureDefinitionValidationException("physical is required");
        }
        if (ai == null) {
            throw new CreatureDefinitionValidationException("ai is required");
        }
        if (states == null) {
            throw new CreatureDefinitionValidationException("states is required");
        }
        if (combat == null) {
            throw new CreatureDefinitionValidationException("combat is required");
        }
        if (ai.usesShield() && !states.enablesState("shield")) {
            throw new CreatureDefinitionValidationException("ai.uses_shield requires enabled shield state");
        }
        if (combat.attackDamage() > 0 && !states.enablesState("attack")) {
            throw new CreatureDefinitionValidationException("combat.attack_damage requires enabled attack state");
        }
    }

    private static void requireText(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new CreatureDefinitionValidationException(field + " must not be blank");
        }
    }

    private static void requireSnakeCase(String field, String value) {
        requireText(field, value);
        if (!value.matches("[a-z][a-z0-9_]*")) {
            throw new CreatureDefinitionValidationException(field + " must be snake_case: " + value);
        }
    }

    private static void requireRelativePath(String field, String value) {
        requireText(field, value);
        if (value.startsWith("/") || value.matches("^[A-Za-z]:.*")) {
            throw new CreatureDefinitionValidationException(field + " must be relative");
        }
    }
}
