package umbra.room;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RoomTriggerResolverTest {
    private final RoomTriggerResolver resolver = new RoomTriggerResolver();

    @Test
    void resolvesDoorTransitionFromActorOverlap() {
        RoomDefinition room = roomWithDoor("forest_test_02", "entry_left");

        Optional<RoomTransitionRequest> request = resolver.findDoorTransition(
                room,
                "forest_test_01",
                120.0f,
                32.0f,
                18.0f,
                38.0f
        );

        assertTrue(request.isPresent());
        assertEquals("forest_test_02", request.get().targetRoomId());
        assertEquals("entry_left", request.get().targetSpawnId());
    }

    @Test
    void resolvesSelfDoorToCurrentRoom() {
        RoomDefinition room = roomWithDoor("self", "entry_left");

        Optional<RoomTransitionRequest> request = resolver.findDoorTransition(
                room,
                "forest_test_01",
                120.0f,
                32.0f,
                18.0f,
                38.0f
        );

        assertTrue(request.isPresent());
        assertEquals("forest_test_01", request.get().targetRoomId());
    }

    @Test
    void resolvesCheckpointTriggerFromActorOverlap() {
        RoomDefinition room = new RoomDefinition(
                "forest_test_01",
                "forest",
                4,
                3,
                32,
                false,
                List.of(new RoomDefinition.TileCell(0, 0)),
                List.of(
                        new RoomDefinition.SpawnPoint("entry_left", "player_spawn", 64.0f, 64.0f),
                        new RoomDefinition.SpawnPoint("checkpoint_a", "checkpoint", 160.0f, 64.0f)
                ),
                List.of(new RoomDefinition.DoorDefinition("right_exit", 120.0f, 32.0f, 8.0f, 64.0f, "self", "entry_left")),
                List.of()
        );

        Optional<String> checkpoint = resolver.findCheckpoint(room, 150.0f, 64.0f, 18.0f, 38.0f, 40.0f, 48.0f);

        assertTrue(checkpoint.isPresent());
        assertEquals("checkpoint_a", checkpoint.get());
    }

    @Test
    void checkpointStateProducesRespawnRequest() {
        CheckpointState state = new CheckpointState("forest_test_01", "entry_left");

        state.activate("forest_test_02", "checkpoint_b");
        RoomTransitionRequest request = state.respawnRequest();

        assertEquals("forest_test_02", request.targetRoomId());
        assertEquals("checkpoint_b", request.targetSpawnId());
    }

    private RoomDefinition roomWithDoor(String targetRoom, String targetSpawn) {
        return new RoomDefinition(
                "forest_test_01",
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
