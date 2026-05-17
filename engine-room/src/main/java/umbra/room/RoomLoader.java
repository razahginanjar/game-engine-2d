package umbra.room;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import umbra.assets.AssetPathValidator;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Loads and validates the engine's room JSON contract. This intentionally stays
 * separate from Tiled so future importers can target the same runtime model.
 */
public final class RoomLoader {
    private static final int V1_TILE_SIZE = 32;

    private final AssetPathValidator pathValidator = new AssetPathValidator();

    public RoomDefinition loadProjectRoom(Path projectAssetRoot, String relativePath) {
        pathValidator.validateProjectRelative(relativePath);
        Path roomPath = projectAssetRoot.resolve(relativePath).normalize();
        if (!roomPath.startsWith(projectAssetRoot.normalize())) {
            throw new RoomValidationException("room path escapes asset root: " + relativePath);
        }
        try (Reader reader = Files.newBufferedReader(roomPath)) {
            return load(reader);
        } catch (IOException exception) {
            throw new RoomValidationException("failed to read room: " + relativePath, exception);
        }
    }

    public RoomDefinition load(Reader reader) {
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        String roomId = requiredString(root, "room_id");
        String biomeId = requiredString(root, "biome_id");
        validateSnakeCase("room_id", roomId);
        validateSnakeCase("biome_id", biomeId);
        int widthTiles = requiredPositiveInt(root, "width_tiles");
        int heightTiles = requiredPositiveInt(root, "height_tiles");
        int tileSize = requiredPositiveInt(root, "tile_size");
        if (tileSize != V1_TILE_SIZE) {
            throw new RoomValidationException("tile_size must be " + V1_TILE_SIZE + " for v1 rooms");
        }
        boolean isolated = optionalBoolean(root, "isolated", false);

        List<RoomDefinition.TileCell> solidTiles = readSolidTiles(root, widthTiles, heightTiles);
        List<RoomDefinition.SpawnPoint> spawns = readSpawns(root, widthTiles, heightTiles, tileSize);
        List<RoomDefinition.DoorDefinition> doors = readDoors(root, widthTiles, heightTiles, tileSize, isolated);
        List<RoomDefinition.CameraZoneDefinition> cameraZones = readCameraZones(root, widthTiles, heightTiles, tileSize);
        validateDoorTargets(doors, spawns);

        return new RoomDefinition(roomId, biomeId, widthTiles, heightTiles, tileSize, isolated, solidTiles, spawns, doors, cameraZones);
    }

    private List<RoomDefinition.TileCell> readSolidTiles(JsonObject root, int widthTiles, int heightTiles) {
        JsonArray cells = requiredArray(root, "solid_tiles");
        List<RoomDefinition.TileCell> result = new ArrayList<>();
        Set<String> uniqueCells = new HashSet<>();
        for (JsonElement element : cells) {
            JsonArray pair = element.getAsJsonArray();
            if (pair.size() != 2) {
                throw new RoomValidationException("solid tile must be [x,y]");
            }
            int x = pair.get(0).getAsInt();
            int y = pair.get(1).getAsInt();
            if (x < 0 || y < 0 || x >= widthTiles || y >= heightTiles) {
                throw new RoomValidationException("solid tile outside room: " + x + "," + y);
            }
            if (uniqueCells.add(x + "," + y)) {
                result.add(new RoomDefinition.TileCell(x, y));
            }
        }
        if (result.isEmpty()) {
            throw new RoomValidationException("room must define at least one solid tile");
        }
        return result;
    }

    private List<RoomDefinition.SpawnPoint> readSpawns(JsonObject root, int widthTiles, int heightTiles, int tileSize) {
        JsonArray spawns = requiredArray(root, "spawns");
        List<RoomDefinition.SpawnPoint> result = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        boolean hasPlayerSpawn = false;
        for (JsonElement element : spawns) {
            JsonObject spawn = element.getAsJsonObject();
            String id = requiredString(spawn, "id");
            String type = requiredString(spawn, "type");
            float x = requiredFloat(spawn, "x");
            float y = requiredFloat(spawn, "y");
            if (!ids.add(id)) {
                throw new RoomValidationException("duplicate spawn id: " + id);
            }
            if (x < 0.0f || y < 0.0f || x >= widthTiles * tileSize || y >= heightTiles * tileSize) {
                throw new RoomValidationException("spawn outside room: " + id);
            }
            if ("player_spawn".equals(type)) {
                hasPlayerSpawn = true;
            }
            result.add(new RoomDefinition.SpawnPoint(id, type, x, y));
        }
        if (!hasPlayerSpawn) {
            throw new RoomValidationException("room must contain at least one player_spawn");
        }
        return result;
    }

    private List<RoomDefinition.DoorDefinition> readDoors(
            JsonObject root,
            int widthTiles,
            int heightTiles,
            int tileSize,
            boolean isolated
    ) {
        if (!root.has("doors")) {
            if (isolated) {
                return List.of();
            }
            throw new RoomValidationException("room must define at least one door unless isolated");
        }
        JsonArray doors = root.getAsJsonArray("doors");
        if (doors.isEmpty() && !isolated) {
            throw new RoomValidationException("room must define at least one door unless isolated");
        }
        if (!doors.isEmpty() && isolated) {
            throw new RoomValidationException("isolated room must not define doors");
        }

        List<RoomDefinition.DoorDefinition> result = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        float roomWidth = widthTiles * tileSize;
        float roomHeight = heightTiles * tileSize;
        for (JsonElement element : doors) {
            JsonObject door = element.getAsJsonObject();
            String id = requiredString(door, "id");
            if (!ids.add(id)) {
                throw new RoomValidationException("duplicate door id: " + id);
            }
            float x = requiredFloat(door, "x");
            float y = requiredFloat(door, "y");
            float width = requiredFloat(door, "w");
            float height = requiredFloat(door, "h");
            validatePositiveArea("door", id, width, height);
            validateRectInsideRoom("door", id, x, y, width, height, roomWidth, roomHeight);
            result.add(new RoomDefinition.DoorDefinition(
                    id,
                    x,
                    y,
                    width,
                    height,
                    requiredString(door, "target_room"),
                    requiredString(door, "target_spawn")
            ));
        }
        return result;
    }

    private List<RoomDefinition.CameraZoneDefinition> readCameraZones(
            JsonObject root,
            int widthTiles,
            int heightTiles,
            int tileSize
    ) {
        if (!root.has("camera_zones")) {
            return List.of();
        }
        JsonArray zones = root.getAsJsonArray("camera_zones");
        List<RoomDefinition.CameraZoneDefinition> result = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        float roomWidth = widthTiles * tileSize;
        float roomHeight = heightTiles * tileSize;
        for (JsonElement element : zones) {
            JsonObject zone = element.getAsJsonObject();
            String id = requiredString(zone, "id");
            if (!ids.add(id)) {
                throw new RoomValidationException("duplicate camera zone id: " + id);
            }
            float x = requiredFloat(zone, "x");
            float y = requiredFloat(zone, "y");
            float width = requiredFloat(zone, "w");
            float height = requiredFloat(zone, "h");
            validatePositiveArea("camera zone", id, width, height);
            validateRectInsideRoom("camera zone", id, x, y, width, height, roomWidth, roomHeight);
            result.add(new RoomDefinition.CameraZoneDefinition(
                    id,
                    x,
                    y,
                    width,
                    height
            ));
        }
        return result;
    }

    private void validateDoorTargets(List<RoomDefinition.DoorDefinition> doors, List<RoomDefinition.SpawnPoint> spawns) {
        Set<String> spawnIds = new HashSet<>();
        for (RoomDefinition.SpawnPoint spawn : spawns) {
            spawnIds.add(spawn.id());
        }
        for (RoomDefinition.DoorDefinition door : doors) {
            if (door.targetRoom().isBlank() || door.targetSpawn().isBlank()) {
                throw new RoomValidationException("door target must not be blank: " + door.id());
            }
            if (door.targetRoom().equals("self") && !spawnIds.contains(door.targetSpawn())) {
                throw new RoomValidationException("self-target door references missing spawn: " + door.id());
            }
        }
    }

    private String requiredString(JsonObject object, String key) {
        if (!object.has(key) || object.get(key).getAsString().isBlank()) {
            throw new RoomValidationException("missing required string: " + key);
        }
        return object.get(key).getAsString();
    }

    private int requiredPositiveInt(JsonObject object, String key) {
        if (!object.has(key)) {
            throw new RoomValidationException("missing required int: " + key);
        }
        int value = object.get(key).getAsInt();
        if (value <= 0) {
            throw new RoomValidationException(key + " must be positive");
        }
        return value;
    }

    private float requiredFloat(JsonObject object, String key) {
        if (!object.has(key)) {
            throw new RoomValidationException("missing required number: " + key);
        }
        return object.get(key).getAsFloat();
    }

    private boolean optionalBoolean(JsonObject object, String key, boolean defaultValue) {
        if (!object.has(key)) {
            return defaultValue;
        }
        return object.get(key).getAsBoolean();
    }

    private JsonArray requiredArray(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonArray()) {
            throw new RoomValidationException("missing required array: " + key);
        }
        return object.getAsJsonArray(key);
    }

    private void validateSnakeCase(String field, String value) {
        if (!value.matches("[a-z][a-z0-9_]*")) {
            throw new RoomValidationException(field + " must be snake_case: " + value);
        }
    }

    private void validatePositiveArea(String type, String id, float width, float height) {
        if (width <= 0.0f || height <= 0.0f) {
            throw new RoomValidationException(type + " size must be positive: " + id);
        }
    }

    private void validateRectInsideRoom(
            String type,
            String id,
            float x,
            float y,
            float width,
            float height,
            float roomWidth,
            float roomHeight
    ) {
        if (x < 0.0f || y < 0.0f || x + width > roomWidth || y + height > roomHeight) {
            throw new RoomValidationException(type + " outside room: " + id);
        }
    }
}
