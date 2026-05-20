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
        boolean isolated,
        List<TileCell> solidTiles,
        List<SpawnPoint> spawns,
        List<DoorDefinition> doors,
        List<CameraZoneDefinition> cameraZones,
        List<AbilityPickupDefinition> abilityPickups,
        List<AbilityGateDefinition> abilityGates,
        List<BossArenaDefinition> bossArenas
) {
    public RoomDefinition(
            String roomId,
            String biomeId,
            int widthTiles,
            int heightTiles,
            int tileSize,
            boolean isolated,
            List<TileCell> solidTiles,
            List<SpawnPoint> spawns,
            List<DoorDefinition> doors,
            List<CameraZoneDefinition> cameraZones
    ) {
        this(roomId, biomeId, widthTiles, heightTiles, tileSize, isolated, solidTiles, spawns, doors, cameraZones, List.of(), List.of(), List.of());
    }

    public RoomDefinition(
            String roomId,
            String biomeId,
            int widthTiles,
            int heightTiles,
            int tileSize,
            boolean isolated,
            List<TileCell> solidTiles,
            List<SpawnPoint> spawns,
            List<DoorDefinition> doors,
            List<CameraZoneDefinition> cameraZones,
            List<AbilityPickupDefinition> abilityPickups,
            List<AbilityGateDefinition> abilityGates
    ) {
        this(roomId, biomeId, widthTiles, heightTiles, tileSize, isolated, solidTiles, spawns, doors, cameraZones, abilityPickups, abilityGates, List.of());
    }

    public RoomDefinition {
        solidTiles = List.copyOf(solidTiles);
        spawns = List.copyOf(spawns);
        doors = List.copyOf(doors);
        cameraZones = List.copyOf(cameraZones);
        abilityPickups = List.copyOf(abilityPickups);
        abilityGates = List.copyOf(abilityGates);
        bossArenas = List.copyOf(bossArenas);
    }

    public record TileCell(int x, int y) {
    }

    public record SpawnPoint(String id, String type, float x, float y) {
    }

    public record DoorDefinition(String id, float x, float y, float width, float height, String targetRoom, String targetSpawn) {
    }

    public record CameraZoneDefinition(String id, float x, float y, float width, float height) {
    }

    public record AbilityPickupDefinition(String id, String abilityId, float x, float y, float width, float height) {
    }

    public record AbilityGateDefinition(String id, String requiredAbilityId, float x, float y, float width, float height) {
    }

    public record BossArenaDefinition(
            String id,
            String bossId,
            String defeatFlagId,
            RectDefinition arena,
            RectDefinition activation,
            List<String> lockedDoorIds
    ) {
        public BossArenaDefinition {
            lockedDoorIds = lockedDoorIds == null ? List.of() : List.copyOf(lockedDoorIds);
        }
    }

    public record RectDefinition(float x, float y, float width, float height) {
    }
}
