package umbra.save;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class SaveGameCodecTest {
    private final SaveGameCodec codec = new SaveGameCodec();

    @Test
    void encodesAndDecodesCheckpointSave() {
        SaveGame saveGame = new SaveGame(
                1,
                "forest_test_02",
                "checkpoint_b",
                List.of("forest_test_01", "forest_test_02"),
                List.of("dash")
        );

        SaveGame decoded = codec.decode(codec.encode(saveGame));

        assertEquals(SaveGame.CURRENT_VERSION, decoded.version());
        assertEquals("forest_test_02", decoded.checkpointRoomId());
        assertEquals("checkpoint_b", decoded.checkpointSpawnId());
        assertEquals(List.of("forest_test_01", "forest_test_02"), decoded.visitedRoomIds());
        assertEquals(List.of("dash"), decoded.unlockedAbilityIds());
    }

    @Test
    void treatsMissingUnlockedAbilitiesAsEmptyForBackwardCompatibility() {
        SaveGame decoded = codec.decode(
                "{\"version\":1,\"checkpointRoomId\":\"forest_test_01\",\"checkpointSpawnId\":\"entry_left\",\"visitedRoomIds\":[\"forest_test_01\"]}"
        );

        assertEquals(List.of(), decoded.unlockedAbilityIds());
    }

    @Test
    void rejectsUnsupportedSaveVersion() {
        assertThrows(SaveValidationException.class,
                () -> codec.decode("{\"version\":99,\"checkpointRoomId\":\"forest_test_01\",\"checkpointSpawnId\":\"entry_left\"}"));
    }

    @Test
    void rejectsBlankCheckpointFields() {
        assertThrows(SaveValidationException.class,
                () -> codec.decode("{\"version\":1,\"checkpointRoomId\":\"\",\"checkpointSpawnId\":\"entry_left\"}"));
    }

    @Test
    void rejectsBlankVisitedRooms() {
        assertThrows(SaveValidationException.class,
                () -> codec.decode("{\"version\":1,\"checkpointRoomId\":\"forest_test_01\",\"checkpointSpawnId\":\"entry_left\",\"visitedRoomIds\":[\"\"]}"));
    }

    @Test
    void rejectsBlankUnlockedAbilities() {
        assertThrows(SaveValidationException.class,
                () -> codec.decode("{\"version\":1,\"checkpointRoomId\":\"forest_test_01\",\"checkpointSpawnId\":\"entry_left\",\"unlockedAbilityIds\":[\"\"]}"));
    }
}
