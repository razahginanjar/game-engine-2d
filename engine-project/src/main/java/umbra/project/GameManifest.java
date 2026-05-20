package umbra.project;

import java.util.List;

public record GameManifest(
        String title,
        String startRoomId,
        String defaultSpawnId,
        String assetRoot,
        SavePolicy savePolicy,
        List<String> enabledModules,
        List<String> creatureDefinitions,
        List<String> roomVisualDefinitions
) {
    public GameManifest {
        requireText("title", title);
        requireSnakeCase("start_room_id", startRoomId);
        requireSnakeCase("default_spawn_id", defaultSpawnId);
        requireText("asset_root", assetRoot);
        if (assetRoot.startsWith("/") || assetRoot.matches("^[A-Za-z]:.*")) {
            throw new GameManifestValidationException("asset_root must be project-relative or workspace-relative");
        }
        if (savePolicy == null) {
            savePolicy = new SavePolicy(false, "");
        }
        enabledModules = enabledModules == null ? List.of() : List.copyOf(enabledModules);
        if (enabledModules.isEmpty()) {
            throw new GameManifestValidationException("enabled_modules must not be empty");
        }
        for (String module : enabledModules) {
            requireSnakeCase("enabled module", module);
        }
        creatureDefinitions = creatureDefinitions == null ? List.of() : List.copyOf(creatureDefinitions);
        for (String definitionPath : creatureDefinitions) {
            requireProjectRelativePath("creature definition path", definitionPath);
        }
        roomVisualDefinitions = roomVisualDefinitions == null ? List.of() : List.copyOf(roomVisualDefinitions);
        for (String definitionPath : roomVisualDefinitions) {
            requireProjectRelativePath("room visual definition path", definitionPath);
        }
    }

    private static void requireText(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new GameManifestValidationException(field + " must not be blank");
        }
    }

    private static void requireSnakeCase(String field, String value) {
        requireText(field, value);
        if (!value.matches("[a-z][a-z0-9_]*")) {
            throw new GameManifestValidationException(field + " must be snake_case: " + value);
        }
    }

    private static void requireProjectRelativePath(String field, String value) {
        requireText(field, value);
        if (value.startsWith("/") || value.matches("^[A-Za-z]:.*")) {
            throw new GameManifestValidationException(field + " must be project-relative");
        }
    }
}
