package umbra.project;

import java.util.List;

public record GameManifest(
        String title,
        String startRoomId,
        String defaultSpawnId,
        String assetRoot,
        SavePolicy savePolicy,
        List<String> enabledModules
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
}
