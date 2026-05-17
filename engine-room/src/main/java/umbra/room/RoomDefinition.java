package umbra.room;

import java.util.List;

/**
 * Validated room definition imported from project JSON. Runtime systems should
 * depend on this internal model instead of editor-specific Tiled objects.
 */
public record RoomDefinition(
        String roomId,
        String biomeId,
        int widthTiles,
        int heightTiles,
        int tileSize,
        List<TileCell> solidTiles,
        List<SpawnPoint> spawns,
        List<DoorDefinition> doors
) {
    public RoomDefinition {
        solidTiles = List.copyOf(solidTiles);
        spawns = List.copyOf(spawns);
        doors = List.copyOf(doors);
    }

    public record TileCell(int x, int y) {
    }

    public record SpawnPoint(String id, String type, float x, float y) {
    }

    public record DoorDefinition(String id, float x, float y, float width, float height, String targetRoom, String targetSpawn) {
    }
}
