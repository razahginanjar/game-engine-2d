package umbra.animation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class AnimationMetadataLoader {
    public AnimationSetDefinition load(Reader reader) {
        Objects.requireNonNull(reader, "reader must not be null");
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        String id = requiredString(root, "id");
        JsonArray clipElements = requiredArray(root, "clips");
        List<AnimationClipDefinition> clips = new ArrayList<>();
        for (JsonElement element : clipElements) {
            clips.add(readClip(element.getAsJsonObject()));
        }
        return new AnimationSetDefinition(id, clips);
    }

    private AnimationClipDefinition readClip(JsonObject object) {
        String textureId = optionalString(object, "texture_id");
        List<String> textureIds = readTextureIds(object);
        return new AnimationClipDefinition(
                requiredString(object, "id"),
                textureId,
                textureIds,
                optionalInt(object, "source_x", 0),
                optionalInt(object, "source_y", 0),
                requiredInt(object, "frame_width"),
                requiredInt(object, "frame_height"),
                requiredInt(object, "frame_count"),
                requiredFloat(object, "fps"),
                requiredBoolean(object, "loop"),
                readEvents(object)
        );
    }

    private List<AnimationFrameEvent> readEvents(JsonObject object) {
        if (!object.has("events")) {
            return List.of();
        }
        JsonArray values = object.getAsJsonArray("events");
        List<AnimationFrameEvent> events = new ArrayList<>();
        for (JsonElement value : values) {
            JsonObject event = value.getAsJsonObject();
            events.add(new AnimationFrameEvent(
                    requiredInt(event, "frame"),
                    requiredString(event, "id")
            ));
        }
        return events;
    }

    private List<String> readTextureIds(JsonObject object) {
        if (!object.has("texture_ids")) {
            return List.of();
        }
        JsonArray values = object.getAsJsonArray("texture_ids");
        List<String> textureIds = new ArrayList<>();
        for (JsonElement value : values) {
            String textureId = value.getAsString();
            if (textureId.isBlank()) {
                throw new AnimationValidationException("texture_ids must not contain blank values");
            }
            textureIds.add(textureId);
        }
        return textureIds;
    }

    private String requiredString(JsonObject object, String key) {
        if (!object.has(key) || object.get(key).getAsString().isBlank()) {
            throw new AnimationValidationException(key + " is required");
        }
        return object.get(key).getAsString();
    }

    private String optionalString(JsonObject object, String key) {
        if (!object.has(key)) {
            return null;
        }
        String value = object.get(key).getAsString();
        return value.isBlank() ? null : value;
    }

    private int requiredInt(JsonObject object, String key) {
        if (!object.has(key)) {
            throw new AnimationValidationException(key + " is required");
        }
        return object.get(key).getAsInt();
    }

    private int optionalInt(JsonObject object, String key, int fallback) {
        return object.has(key) ? object.get(key).getAsInt() : fallback;
    }

    private float requiredFloat(JsonObject object, String key) {
        if (!object.has(key)) {
            throw new AnimationValidationException(key + " is required");
        }
        return object.get(key).getAsFloat();
    }

    private boolean requiredBoolean(JsonObject object, String key) {
        if (!object.has(key)) {
            throw new AnimationValidationException(key + " is required");
        }
        return object.get(key).getAsBoolean();
    }

    private JsonArray requiredArray(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonArray()) {
            throw new AnimationValidationException(key + " is required");
        }
        return object.getAsJsonArray(key);
    }
}
