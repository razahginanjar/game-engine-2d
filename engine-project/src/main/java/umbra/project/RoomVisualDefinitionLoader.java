package umbra.project;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class RoomVisualDefinitionLoader {
    public RoomVisualDefinition loadProjectRoomVisual(Path projectRoot, String relativePath) {
        Path normalizedRoot = projectRoot.toAbsolutePath().normalize();
        Path visualPath = normalizedRoot.resolve(relativePath).normalize();
        if (!visualPath.startsWith(normalizedRoot)) {
            throw new RoomVisualDefinitionValidationException("room visual path escapes project root: " + relativePath);
        }
        try (Reader reader = Files.newBufferedReader(visualPath)) {
            return load(reader);
        } catch (IOException exception) {
            throw new RoomVisualDefinitionValidationException("failed to read room visual definition: " + relativePath, exception);
        }
    }

    public RoomVisualDefinition load(Reader reader) {
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        return new RoomVisualDefinition(
                requiredString(root, "room_id"),
                optionalString(root, "ambient_color", "#FFFFFFFF"),
                readLayers(root)
        );
    }

    private List<RoomVisualLayerDefinition> readLayers(JsonObject root) {
        JsonArray layers = requiredArray(root, "layers");
        List<RoomVisualLayerDefinition> result = new ArrayList<>();
        for (JsonElement element : layers) {
            JsonObject layer = element.getAsJsonObject();
            result.add(new RoomVisualLayerDefinition(
                    requiredString(layer, "id"),
                    requiredString(layer, "type"),
                    requiredString(layer, "asset_path"),
                    requiredInt(layer, "order"),
                    optionalFloat(layer, "parallax_x", 1.0f),
                    optionalFloat(layer, "parallax_y", 1.0f),
                    optionalString(layer, "repeat_mode", "none"),
                    optionalFloat(layer, "offset_x", 0.0f),
                    optionalFloat(layer, "offset_y", 0.0f),
                    optionalFloat(layer, "scale_x", 1.0f),
                    optionalFloat(layer, "scale_y", 1.0f),
                    optionalFloat(layer, "opacity", 1.0f),
                    optionalString(layer, "tint", "#FFFFFFFF")
            ));
        }
        return result;
    }

    private String requiredString(JsonObject object, String key) {
        if (!object.has(key) || object.get(key).getAsString().isBlank()) {
            throw new RoomVisualDefinitionValidationException("missing required string: " + key);
        }
        return object.get(key).getAsString();
    }

    private String optionalString(JsonObject object, String key, String fallback) {
        if (!object.has(key)) {
            return fallback;
        }
        String value = object.get(key).getAsString();
        return value.isBlank() ? fallback : value;
    }

    private int requiredInt(JsonObject object, String key) {
        if (!object.has(key)) {
            throw new RoomVisualDefinitionValidationException("missing required int: " + key);
        }
        return object.get(key).getAsInt();
    }

    private float optionalFloat(JsonObject object, String key, float fallback) {
        return object.has(key) ? object.get(key).getAsFloat() : fallback;
    }

    private JsonArray requiredArray(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonArray()) {
            throw new RoomVisualDefinitionValidationException("missing required array: " + key);
        }
        return object.getAsJsonArray(key);
    }
}
