package umbra.boss;

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

public final class BossDefinitionLoader {
    public BossDefinition loadProjectBoss(Path projectAssetRoot, String relativePath) {
        Path normalizedRoot = projectAssetRoot.normalize();
        Path bossPath = normalizedRoot.resolve(relativePath).normalize();
        if (!bossPath.startsWith(normalizedRoot)) {
            throw new BossDefinitionValidationException("boss path escapes asset root: " + relativePath);
        }
        try (Reader reader = Files.newBufferedReader(bossPath)) {
            return load(reader);
        } catch (IOException exception) {
            throw new BossDefinitionValidationException("failed to read boss definition: " + relativePath, exception);
        }
    }

    public BossDefinition load(Reader reader) {
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        return new BossDefinition(
                requiredString(root, "id"),
                requiredString(root, "display_name"),
                requiredPositiveInt(root, "max_health"),
                readPhases(root),
                readAttacks(root)
        );
    }

    private List<BossPhaseDefinition> readPhases(JsonObject root) {
        JsonArray phases = requiredArray(root, "phases");
        List<BossPhaseDefinition> result = new ArrayList<>();
        for (JsonElement element : phases) {
            JsonObject phase = element.getAsJsonObject();
            result.add(new BossPhaseDefinition(
                    requiredString(phase, "id"),
                    requiredFloat(phase, "starts_at_health_ratio")
            ));
        }
        return result;
    }

    private List<BossAttackDefinition> readAttacks(JsonObject root) {
        JsonArray attacks = requiredArray(root, "attacks");
        List<BossAttackDefinition> result = new ArrayList<>();
        for (JsonElement element : attacks) {
            JsonObject attack = element.getAsJsonObject();
            result.add(new BossAttackDefinition(
                    requiredString(attack, "id"),
                    requiredString(attack, "phase_id"),
                    requiredString(attack, "clip_id"),
                    requiredString(attack, "hitbox_profile"),
                    requiredFloat(attack, "min_range"),
                    requiredFloat(attack, "max_range"),
                    requiredFloat(attack, "cooldown_seconds"),
                    requiredPositiveInt(attack, "frame_count"),
                    requiredFloat(attack, "fps"),
                    requiredPositiveInt(attack, "damage"),
                    requiredFloat(attack, "knockback_x"),
                    requiredFloat(attack, "knockback_y"),
                    requiredFloat(attack, "hit_pause_seconds"),
                    requiredFloat(attack, "hit_stun_seconds"),
                    optionalString(attack, "damage_type", "physical")
            ));
        }
        return result;
    }

    private String requiredString(JsonObject object, String key) {
        if (!object.has(key) || object.get(key).getAsString().isBlank()) {
            throw new BossDefinitionValidationException("missing required string: " + key);
        }
        return object.get(key).getAsString();
    }

    private String optionalString(JsonObject object, String key, String defaultValue) {
        if (!object.has(key) || object.get(key).getAsString().isBlank()) {
            return defaultValue;
        }
        return object.get(key).getAsString();
    }

    private int requiredPositiveInt(JsonObject object, String key) {
        if (!object.has(key)) {
            throw new BossDefinitionValidationException("missing required int: " + key);
        }
        int value = object.get(key).getAsInt();
        if (value <= 0) {
            throw new BossDefinitionValidationException(key + " must be positive");
        }
        return value;
    }

    private float requiredFloat(JsonObject object, String key) {
        if (!object.has(key)) {
            throw new BossDefinitionValidationException("missing required number: " + key);
        }
        return object.get(key).getAsFloat();
    }

    private JsonArray requiredArray(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonArray()) {
            throw new BossDefinitionValidationException("missing required array: " + key);
        }
        return object.getAsJsonArray(key);
    }
}
