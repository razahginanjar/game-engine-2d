package umbra.animation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import umbra.assets.AssetPathValidator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Loads and validates JSON animation metadata before gameplay code requests
 * animation keys. This prevents hardcoded frame paths inside movement/combat.
 */
public final class AnimationMetadataLoader {
    private final AssetPathValidator pathValidator = new AssetPathValidator();

    public AnimationSetDefinition load(Reader reader) {
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        AnimationSetDefinition definition = new AnimationSetDefinition(
                requiredString(root, "asset_id"),
                requiredPositiveInt(root, "frame_width"),
                requiredPositiveInt(root, "frame_height"),
                requiredString(root, "pivot"),
                readOptionalRect(root, "collision_box"),
                readOptionalRect(root, "hurtbox"),
                readClips(root)
        );
        validateUniqueClipIds(definition);
        return definition;
    }

    public void validateSheets(AnimationSetDefinition definition, Path assetRoot, Set<String> requiredClipIds) {
        Path normalizedRoot = assetRoot.normalize();
        for (String requiredClipId : requiredClipIds) {
            if (definition.clip(requiredClipId).isEmpty()) {
                throw new AnimationValidationException("missing required clip: " + requiredClipId);
            }
        }
        for (AnimationClipDefinition clip : definition.clips()) {
            pathValidator.validateProjectRelative(clip.sheetPath());
            Path sheetPath = normalizedRoot.resolve(clip.sheetPath()).normalize();
            if (!sheetPath.startsWith(normalizedRoot)) {
                throw new AnimationValidationException("clip sheet escapes asset root: " + clip.id());
            }
            if (!Files.exists(sheetPath)) {
                throw new AnimationValidationException("missing sheet for clip " + clip.id() + ": " + clip.sheetPath());
            }
            validateSheetDimensions(definition, clip, sheetPath);
        }
    }

    private List<AnimationClipDefinition> readClips(JsonObject root) {
        JsonArray clips = requiredArray(root, "clips");
        List<AnimationClipDefinition> result = new ArrayList<>();
        for (JsonElement element : clips) {
            JsonObject clip = element.getAsJsonObject();
            result.add(new AnimationClipDefinition(
                    requiredString(clip, "id"),
                    requiredString(clip, "sheet"),
                    requiredPositiveInt(clip, "frames"),
                    requiredPositiveInt(clip, "fps"),
                    requiredBoolean(clip, "loop")
            ));
        }
        return result;
    }

    private RectDefinition readOptionalRect(JsonObject root, String key) {
        if (!root.has(key)) {
            return null;
        }
        JsonObject rect = root.getAsJsonObject(key);
        return new RectDefinition(
                requiredInt(rect, "x"),
                requiredInt(rect, "y"),
                requiredPositiveInt(rect, "w"),
                requiredPositiveInt(rect, "h")
        );
    }

    private void validateUniqueClipIds(AnimationSetDefinition definition) {
        Set<String> ids = new HashSet<>();
        for (AnimationClipDefinition clip : definition.clips()) {
            if (!ids.add(clip.id())) {
                throw new AnimationValidationException("duplicate clip id: " + clip.id());
            }
        }
    }

    private void validateSheetDimensions(AnimationSetDefinition definition, AnimationClipDefinition clip, Path sheetPath) {
        try {
            BufferedImage image = ImageIO.read(sheetPath.toFile());
            if (image == null) {
                throw new AnimationValidationException("unsupported image file for clip: " + clip.id());
            }
            int expectedWidth = definition.frameWidth() * clip.frameCount();
            if (image.getWidth() != expectedWidth || image.getHeight() != definition.frameHeight()) {
                throw new AnimationValidationException(
                        "sheet dimensions mismatch for clip " + clip.id()
                                + ": expected " + expectedWidth + "x" + definition.frameHeight()
                                + " but got " + image.getWidth() + "x" + image.getHeight()
                );
            }
        } catch (IOException exception) {
            throw new AnimationValidationException("failed to read sheet for clip: " + clip.id(), exception);
        }
    }

    private String requiredString(JsonObject object, String key) {
        if (!object.has(key) || object.get(key).getAsString().isBlank()) {
            throw new AnimationValidationException("missing required string: " + key);
        }
        return object.get(key).getAsString();
    }

    private int requiredInt(JsonObject object, String key) {
        if (!object.has(key)) {
            throw new AnimationValidationException("missing required int: " + key);
        }
        return object.get(key).getAsInt();
    }

    private int requiredPositiveInt(JsonObject object, String key) {
        int value = requiredInt(object, key);
        if (value <= 0) {
            throw new AnimationValidationException(key + " must be positive");
        }
        return value;
    }

    private boolean requiredBoolean(JsonObject object, String key) {
        if (!object.has(key)) {
            throw new AnimationValidationException("missing required boolean: " + key);
        }
        return object.get(key).getAsBoolean();
    }

    private JsonArray requiredArray(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonArray()) {
            throw new AnimationValidationException("missing required array: " + key);
        }
        return object.getAsJsonArray(key);
    }
}
