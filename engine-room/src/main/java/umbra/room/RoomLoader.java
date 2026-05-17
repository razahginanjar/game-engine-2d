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
        int widthTiles = requiredPositiveInt(root, "width_tiles");
        int heightTiles = requiredPositiveInt(root, "height_tiles");
        int tileSize = requiredPositiveInt(root, "tile_size");

        List<RoomDefinition.TileCell> solidTiles = readSolidTiles(root, widthTiles, heightTiles);
        List<RoomDefinition.SpawnPoint> spawns = readSpawns(root, widthTiles, heightTiles, tileSize);
        List<RoomDefinition.DoorDefinition> doors = readDoors(root);
        validateDoorTargets(doors, spawns);

        return new RoomDefinition(roomId, biomeId, widthTiles, heightTiles, tileSize, solidTiles, spawns, doors);
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

    private List<RoomDefinition.DoorDefinition> readDoors(JsonObject root) {
        if (!root.has("doors")) {
            return List.of();
        }
        JsonArray doors = root.getAsJsonArray("doors");
        List<RoomDefinition.DoorDefinition> result = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (JsonElement element : doors) {
            JsonObject door = element.getAsJsonObject();
            String id = requiredString(door, "id");
            if (!ids.add(id)) {
                throw new RoomValidationException("duplicate door id: " + id);
            }
            result.add(new RoomDefinition.DoorDefinition(
                    id,
                    requiredFloat(door, "x"),
                    requiredFloat(door, "y"),
                    requiredFloat(door, "w"),
                    requiredFloat(door, "h"),
                    requiredString(door, "target_room"),
                    requiredString(door, "target_spawn")
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

    private JsonArray requiredArray(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonArray()) {
            throw new RoomValidationException("missing required array: " + key);
        }
        return object.getAsJsonArray(key);
    }
}
