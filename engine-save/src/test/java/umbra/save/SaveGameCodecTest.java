package umbra.save;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class SaveGameCodecTest {
    private final SaveGameCodec codec = new SaveGameCodec();

    @Test
    void encodesAndDecodesCheckpointSave() {
        SaveGame saveGame = new SaveGame("forest_test_02", "checkpoint_b");

        SaveGame decoded = codec.decode(codec.encode(saveGame));

        assertEquals(SaveGame.CURRENT_VERSION, decoded.version());
        assertEquals("forest_test_02", decoded.checkpointRoomId());
        assertEquals("checkpoint_b", decoded.checkpointSpawnId());
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
}
