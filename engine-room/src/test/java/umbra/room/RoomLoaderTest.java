package umbra.room;

import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class RoomLoaderTest {
    @Test
    void loadsValidRoomDefinition() {
        RoomDefinition room = new RoomLoader().load(new StringReader("""
                {
                  "room_id": "forest_test_01",
                  "biome_id": "forest",
                  "width_tiles": 4,
                  "height_tiles": 3,
                  "tile_size": 32,
                  "solid_tiles": [[0,0],[1,0],[2,0],[3,0]],
                  "spawns": [{"id":"entry_left","type":"player_spawn","x":64,"y":64}],
                  "doors": [{"id":"right_exit","x":120,"y":32,"w":8,"h":64,"target_room":"self","target_spawn":"entry_left"}]
                }
                """));

        assertEquals("forest_test_01", room.roomId());
        assertEquals(4, room.solidTiles().size());
        assertEquals(1, room.spawns().size());
        assertEquals(1, room.doors().size());
        assertEquals(0, room.cameraZones().size());
    }

    @Test
    void rejectsRoomWithoutPlayerSpawn() {
        RoomLoader loader = new RoomLoader();

        assertThrows(RoomValidationException.class, () -> loader.load(new StringReader("""
                {
                  "room_id": "broken",
                  "biome_id": "forest",
                  "width_tiles": 4,
                  "height_tiles": 3,
                  "tile_size": 32,
                  "solid_tiles": [[0,0]],
                  "spawns": []
                }
                """)));
    }

    @Test
    void rejectsSolidTileOutsideRoom() {
        RoomLoader loader = new RoomLoader();

        assertThrows(RoomValidationException.class, () -> loader.load(new StringReader("""
                {
                  "room_id": "broken",
                  "biome_id": "forest",
                  "width_tiles": 4,
                  "height_tiles": 3,
                  "tile_size": 32,
                  "solid_tiles": [[7,0]],
                  "spawns": [{"id":"entry_left","type":"player_spawn","x":64,"y":64}]
                }
                """)));
    }

    @Test
    void loadsIsolatedRoomWithoutDoors() {
        RoomDefinition room = new RoomLoader().load(new StringReader("""
                {
                  "room_id": "boss_test",
                  "biome_id": "forest",
                  "width_tiles": 4,
                  "height_tiles": 3,
                  "tile_size": 32,
                  "isolated": true,
                  "solid_tiles": [[0,0],[1,0],[2,0],[3,0]],
                  "spawns": [{"id":"entry_left","type":"player_spawn","x":64,"y":64}]
                }
                """));

        assertEquals(true, room.isolated());
        assertEquals(0, room.doors().size());
    }

    @Test
    void rejectsNonIsolatedRoomWithoutDoors() {
        RoomLoader loader = new RoomLoader();

        assertThrows(RoomValidationException.class, () -> loader.load(new StringReader("""
                {
                  "room_id": "broken",
                  "biome_id": "forest",
                  "width_tiles": 4,
                  "height_tiles": 3,
                  "tile_size": 32,
                  "solid_tiles": [[0,0]],
                  "spawns": [{"id":"entry_left","type":"player_spawn","x":64,"y":64}]
                }
                """)));
    }

    @Test
    void rejectsDoorOutsideRoom() {
        RoomLoader loader = new RoomLoader();

        assertThrows(RoomValidationException.class, () -> loader.load(new StringReader("""
                {
                  "room_id": "broken",
                  "biome_id": "forest",
                  "width_tiles": 4,
                  "height_tiles": 3,
                  "tile_size": 32,
                  "solid_tiles": [[0,0]],
                  "spawns": [{"id":"entry_left","type":"player_spawn","x":64,"y":64}],
                  "doors": [{"id":"right_exit","x":128,"y":32,"w":8,"h":64,"target_room":"self","target_spawn":"entry_left"}]
                }
                """)));
    }

    @Test
    void loadsCameraZones() {
        RoomDefinition room = new RoomLoader().load(new StringReader("""
                {
                  "room_id": "forest_test_01",
                  "biome_id": "forest",
                  "width_tiles": 4,
                  "height_tiles": 3,
                  "tile_size": 32,
                  "solid_tiles": [[0,0],[1,0],[2,0],[3,0]],
                  "spawns": [{"id":"entry_left","type":"player_spawn","x":64,"y":64}],
                  "doors": [{"id":"right_exit","x":120,"y":32,"w":8,"h":64,"target_room":"self","target_spawn":"entry_left"}],
                  "camera_zones": [{"id":"main","x":0,"y":0,"w":128,"h":96}]
                }
                """));

        assertEquals(1, room.cameraZones().size());
        assertEquals("main", room.cameraZones().get(0).id());
    }

    @Test
    void rejectsInvalidTileSizeForV1() {
        RoomLoader loader = new RoomLoader();

        assertThrows(RoomValidationException.class, () -> loader.load(new StringReader("""
                {
                  "room_id": "broken",
                  "biome_id": "forest",
                  "width_tiles": 4,
                  "height_tiles": 3,
                  "tile_size": 16,
                  "solid_tiles": [[0,0]],
                  "spawns": [{"id":"entry_left","type":"player_spawn","x":64,"y":64}],
                  "doors": [{"id":"right_exit","x":120,"y":32,"w":8,"h":64,"target_room":"self","target_spawn":"entry_left"}]
                }
                """)));
    }

    @Test
    void rejectsNonSnakeCaseObjectIds() {
        RoomLoader loader = new RoomLoader();

        assertThrows(RoomValidationException.class, () -> loader.load(new StringReader("""
                {
                  "room_id": "broken",
                  "biome_id": "forest",
                  "width_tiles": 4,
                  "height_tiles": 3,
                  "tile_size": 32,
                  "solid_tiles": [[0,0]],
                  "spawns": [{"id":"EntryLeft","type":"player_spawn","x":64,"y":64}],
                  "doors": [{"id":"right_exit","x":120,"y":32,"w":8,"h":64,"target_room":"self","target_spawn":"EntryLeft"}]
                }
                """)));
    }

    @Test
    void rejectsNonSnakeCaseDoorTargetRoom() {
        RoomLoader loader = new RoomLoader();

        assertThrows(RoomValidationException.class, () -> loader.load(new StringReader("""
                {
                  "room_id": "broken",
                  "biome_id": "forest",
                  "width_tiles": 4,
                  "height_tiles": 3,
                  "tile_size": 32,
                  "solid_tiles": [[0,0]],
                  "spawns": [{"id":"entry_left","type":"player_spawn","x":64,"y":64}],
                  "doors": [{"id":"right_exit","x":120,"y":32,"w":8,"h":64,"target_room":"Forest02","target_spawn":"entry_left"}]
                }
                """)));
    }
}
