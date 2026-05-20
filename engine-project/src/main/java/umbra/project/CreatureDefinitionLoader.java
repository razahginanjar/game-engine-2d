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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CreatureDefinitionLoader {
    public CreatureDefinition loadProjectCreature(Path projectRoot, String relativePath) {
        Path normalizedRoot = projectRoot.toAbsolutePath().normalize();
        Path creaturePath = normalizedRoot.resolve(relativePath).normalize();
        if (!creaturePath.startsWith(normalizedRoot)) {
            throw new CreatureDefinitionValidationException("creature path escapes project root: " + relativePath);
        }
        try (Reader reader = Files.newBufferedReader(creaturePath)) {
            return load(reader);
        } catch (IOException exception) {
            throw new CreatureDefinitionValidationException("failed to read creature definition: " + relativePath, exception);
        }
    }

    public CreatureDefinition load(Reader reader) {
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        return new CreatureDefinition(
                requiredString(root, "id"),
                requiredString(root, "display_name"),
                requiredString(root, "category"),
                requiredString(root, "asset_root"),
                requiredString(root, "animation_metadata"),
                readStringArray(root, "tags", false),
                readPhysical(root),
                readAi(root),
                readStates(root),
                readCombat(root)
        );
    }

    private CreaturePhysicalSetup readPhysical(JsonObject root) {
        JsonObject physical = requiredObject(root, "physical");
        return new CreaturePhysicalSetup(
                requiredFloat(physical, "body_width"),
                requiredFloat(physical, "body_height"),
                requiredFloat(physical, "hurtbox_width"),
                requiredFloat(physical, "hurtbox_height"),
                requiredFloat(physical, "movement_speed"),
                optionalBoolean(physical, "flying", false)
        );
    }

    private CreatureAiProfile readAi(JsonObject root) {
        JsonObject ai = requiredObject(root, "ai");
        return new CreatureAiProfile(
                requiredString(ai, "profile"),
                requiredFloat(ai, "vision_range"),
                requiredFloat(ai, "caution_range"),
                requiredFloat(ai, "attack_range"),
                optionalFloat(ai, "evade_probability", 0.0f),
                optionalBoolean(ai, "uses_shield", false)
        );
    }

    private CreatureStateModel readStates(JsonObject root) {
        JsonObject states = requiredObject(root, "states");
        return new CreatureStateModel(
                readStringArray(states, "required", true),
                readStringArray(states, "optional", false),
                readStringArray(states, "disabled", false),
                readStringMap(states, "animation_mapping")
        );
    }

    private CreatureCombatProfile readCombat(JsonObject root) {
        JsonObject combat = requiredObject(root, "combat");
        return new CreatureCombatProfile(
                requiredInt(combat, "max_health"),
                optionalInt(combat, "contact_damage", 0),
                optionalInt(combat, "attack_damage", 0),
                optionalString(combat, "attack_active_event", "")
        );
    }

    private JsonObject requiredObject(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonObject()) {
            throw new CreatureDefinitionValidationException("missing required object: " + key);
        }
        return object.getAsJsonObject(key);
    }

    private List<String> readStringArray(JsonObject object, String key, boolean required) {
        if (!object.has(key)) {
            if (required) {
                throw new CreatureDefinitionValidationException("missing required array: " + key);
            }
            return List.of();
        }
        if (!object.get(key).isJsonArray()) {
            throw new CreatureDefinitionValidationException(key + " must be an array");
        }
        JsonArray values = object.getAsJsonArray(key);
        List<String> result = new ArrayList<>();
        for (JsonElement value : values) {
            result.add(value.getAsString());
        }
        return result;
    }

    private Map<String, String> readStringMap(JsonObject object, String key) {
        if (!object.has(key)) {
            return Map.of();
        }
        if (!object.get(key).isJsonObject()) {
            throw new CreatureDefinitionValidationException(key + " must be an object");
        }
        JsonObject values = object.getAsJsonObject(key);
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getAsString());
        }
        return result;
    }

    private String requiredString(JsonObject object, String key) {
        if (!object.has(key) || object.get(key).getAsString().isBlank()) {
            throw new CreatureDefinitionValidationException("missing required string: " + key);
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
            throw new CreatureDefinitionValidationException("missing required int: " + key);
        }
        return object.get(key).getAsInt();
    }

    private int optionalInt(JsonObject object, String key, int fallback) {
        return object.has(key) ? object.get(key).getAsInt() : fallback;
    }

    private float requiredFloat(JsonObject object, String key) {
        if (!object.has(key)) {
            throw new CreatureDefinitionValidationException("missing required number: " + key);
        }
        return object.get(key).getAsFloat();
    }

    private float optionalFloat(JsonObject object, String key, float fallback) {
        return object.has(key) ? object.get(key).getAsFloat() : fallback;
    }

    private boolean optionalBoolean(JsonObject object, String key, boolean fallback) {
        return object.has(key) ? object.get(key).getAsBoolean() : fallback;
    }
}
