package umbra.project;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record RoomVisualDefinition(
        String roomId,
        String ambientColor,
        List<RoomVisualLayerDefinition> layers
) {
    public RoomVisualDefinition {
        requireSnakeCase("room_id", roomId);
        RoomVisualLayerDefinition.requireHexColor("ambient_color", ambientColor);
        layers = layers == null ? List.of() : List.copyOf(layers);
        if (layers.isEmpty()) {
            throw new RoomVisualDefinitionValidationException("room visual definition must contain at least one layer");
        }
        Set<String> ids = new HashSet<>();
        Set<Integer> orders = new HashSet<>();
        for (RoomVisualLayerDefinition layer : layers) {
            if (!ids.add(layer.id())) {
                throw new RoomVisualDefinitionValidationException("duplicate visual layer id: " + layer.id());
            }
            if (!orders.add(layer.order())) {
                throw new RoomVisualDefinitionValidationException("duplicate visual layer order: " + layer.order());
            }
        }
    }

    private static void requireSnakeCase(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new RoomVisualDefinitionValidationException(field + " must not be blank");
        }
        if (!value.matches("[a-z][a-z0-9_]*")) {
            throw new RoomVisualDefinitionValidationException(field + " must be snake_case: " + value);
        }
    }
}
