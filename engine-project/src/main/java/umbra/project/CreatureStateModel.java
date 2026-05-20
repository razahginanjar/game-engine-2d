package umbra.project;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record CreatureStateModel(
        List<String> requiredStates,
        List<String> optionalStates,
        List<String> disabledStates,
        Map<String, String> animationMapping
) {
    public CreatureStateModel {
        requiredStates = copyStateList("states.required", requiredStates);
        optionalStates = copyStateList("states.optional", optionalStates);
        disabledStates = copyStateList("states.disabled", disabledStates);
        animationMapping = animationMapping == null ? Map.of() : Map.copyOf(animationMapping);
        if (requiredStates.isEmpty()) {
            throw new CreatureDefinitionValidationException("states.required must not be empty");
        }
        Set<String> enabledStates = new LinkedHashSet<>();
        enabledStates.addAll(requiredStates);
        enabledStates.addAll(optionalStates);
        for (String disabledState : disabledStates) {
            if (enabledStates.contains(disabledState)) {
                throw new CreatureDefinitionValidationException("state cannot be both enabled and disabled: " + disabledState);
            }
        }
        for (Map.Entry<String, String> entry : animationMapping.entrySet()) {
            requireSnakeCase("animation mapping state", entry.getKey());
            requireSnakeCase("animation mapping clip", entry.getValue());
            if (disabledStates.contains(entry.getKey())) {
                throw new CreatureDefinitionValidationException("disabled state must not define animation mapping: " + entry.getKey());
            }
        }
    }

    public boolean enablesState(String state) {
        return requiredStates.contains(state) || optionalStates.contains(state);
    }

    private static List<String> copyStateList(String field, List<String> states) {
        List<String> result = states == null ? List.of() : List.copyOf(states);
        for (String state : result) {
            requireSnakeCase(field, state);
        }
        return result;
    }

    private static void requireSnakeCase(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new CreatureDefinitionValidationException(field + " must not contain blank values");
        }
        if (!value.matches("[a-z][a-z0-9_]*")) {
            throw new CreatureDefinitionValidationException(field + " must be snake_case: " + value);
        }
    }
}
