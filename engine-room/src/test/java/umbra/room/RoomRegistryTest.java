package umbra.room;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class RoomRegistryTest {
    @Test
    void resolvesCrossRoomDoorTargets() {
        RoomDefinition first = room("forest_test_01", "forest_test_02", "entry_left");
        RoomDefinition second = room("forest_test_02", "forest_test_01", "entry_left");

        RoomRegistry registry = new RoomRegistry(List.of(first, second));

        assertEquals(second, registry.room("forest_test_02"));
    }

    @Test
    void rejectsDoorTargetingMissingRoom() {
        RoomDefinition room = room("forest_test_01", "forest_missing", "entry_left");

        assertThrows(RoomValidationException.class, () -> new RoomRegistry(List.of(room)));
    }

    @Test
    void rejectsDoorTargetingMissingSpawn() {
        RoomDefinition first = room("forest_test_01", "forest_test_02", "missing_spawn");
        RoomDefinition second = room("forest_test_02", "forest_test_01", "entry_left");

        assertThrows(RoomValidationException.class, () -> new RoomRegistry(List.of(first, second)));
    }

    private RoomDefinition room(String id, String targetRoom, String targetSpawn) {
        return new RoomDefinition(
                id,
                "forest",
                4,
                3,
                32,
                false,
                List.of(new RoomDefinition.TileCell(0, 0)),
                List.of(new RoomDefinition.SpawnPoint("entry_left", "player_spawn", 64.0f, 64.0f)),
                List.of(new RoomDefinition.DoorDefinition("right_exit", 120.0f, 32.0f, 8.0f, 64.0f, targetRoom, targetSpawn)),
                List.of()
        );
    }
}
