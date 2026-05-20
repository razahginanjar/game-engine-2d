package umbra.project;

import java.util.Set;

public record RoomVisualLayerDefinition(
        String id,
        String type,
        String assetPath,
        int order,
        float parallaxX,
        float parallaxY,
        String repeatMode,
        float offsetX,
        float offsetY,
        float scaleX,
        float scaleY,
        float opacity,
        String tint
) {
    private static final Set<String> TYPES = Set.of("background", "foreground");
    private static final Set<String> REPEAT_MODES = Set.of("none", "repeat_x", "repeat_y", "repeat_xy");

    public RoomVisualLayerDefinition {
        requireSnakeCase("layer id", id);
        if (!TYPES.contains(type)) {
            throw new RoomVisualDefinitionValidationException("layer type must be background or foreground: " + id);
        }
        requireRelativePath("layer asset_path", assetPath);
        if (parallaxX < 0.0f || parallaxY < 0.0f) {
            throw new RoomVisualDefinitionValidationException("parallax values must not be negative: " + id);
        }
        if (!REPEAT_MODES.contains(repeatMode)) {
            throw new RoomVisualDefinitionValidationException("unsupported repeat_mode: " + repeatMode);
        }
        if (scaleX <= 0.0f || scaleY <= 0.0f) {
            throw new RoomVisualDefinitionValidationException("scale values must be positive: " + id);
        }
        if (opacity < 0.0f || opacity > 1.0f) {
            throw new RoomVisualDefinitionValidationException("opacity must be between 0 and 1: " + id);
        }
        requireHexColor("layer tint", tint);
    }

    private static void requireSnakeCase(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new RoomVisualDefinitionValidationException(field + " must not be blank");
        }
        if (!value.matches("[a-z][a-z0-9_]*")) {
            throw new RoomVisualDefinitionValidationException(field + " must be snake_case: " + value);
        }
    }

    private static void requireRelativePath(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new RoomVisualDefinitionValidationException(field + " must not be blank");
        }
        if (value.startsWith("/") || value.matches("^[A-Za-z]:.*")) {
            throw new RoomVisualDefinitionValidationException(field + " must be relative");
        }
    }

    static void requireHexColor(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new RoomVisualDefinitionValidationException(field + " must not be blank");
        }
        if (!value.matches("#[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?")) {
            throw new RoomVisualDefinitionValidationException(field + " must be #RRGGBB or #RRGGBBAA: " + value);
        }
    }
}
