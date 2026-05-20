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

public final class GameManifestLoader {
    public GameManifest loadProjectManifest(Path projectRoot, String relativePath) {
        Path normalizedRoot = projectRoot.toAbsolutePath().normalize();
        Path manifestPath = normalizedRoot.resolve(relativePath).normalize();
        if (!manifestPath.startsWith(normalizedRoot)) {
            throw new GameManifestValidationException("manifest path escapes project root: " + relativePath);
        }
        try (Reader reader = Files.newBufferedReader(manifestPath)) {
            return load(reader);
        } catch (IOException exception) {
            throw new GameManifestValidationException("failed to read game manifest: " + relativePath, exception);
        }
    }

    public GameManifest load(Reader reader) {
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        return new GameManifest(
                requiredString(root, "title"),
                requiredString(root, "start_room_id"),
                requiredString(root, "default_spawn_id"),
                requiredString(root, "asset_root"),
                readSavePolicy(root),
                readEnabledModules(root)
        );
    }

    private SavePolicy readSavePolicy(JsonObject root) {
        if (!root.has("save") || !root.get("save").isJsonObject()) {
            return new SavePolicy(false, "");
        }
        JsonObject save = root.getAsJsonObject("save");
        boolean enabled = save.has("enabled") && save.get("enabled").getAsBoolean();
        String path = save.has("path") ? save.get("path").getAsString() : "";
        return new SavePolicy(enabled, path);
    }

    private List<String> readEnabledModules(JsonObject root) {
        JsonArray modules = requiredArray(root, "enabled_modules");
        List<String> result = new ArrayList<>();
        for (JsonElement module : modules) {
            result.add(module.getAsString());
        }
        return result;
    }

    private String requiredString(JsonObject object, String key) {
        if (!object.has(key) || object.get(key).getAsString().isBlank()) {
            throw new GameManifestValidationException("missing required string: " + key);
        }
        return object.get(key).getAsString();
    }

    private JsonArray requiredArray(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonArray()) {
            throw new GameManifestValidationException("missing required array: " + key);
        }
        return object.getAsJsonArray(key);
    }
}
