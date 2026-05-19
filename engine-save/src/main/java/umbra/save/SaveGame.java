package umbra.save;

public record SaveGame(
        int version,
        String checkpointRoomId,
        String checkpointSpawnId
) {
    public static final int CURRENT_VERSION = 1;

    public SaveGame(String checkpointRoomId, String checkpointSpawnId) {
        this(CURRENT_VERSION, checkpointRoomId, checkpointSpawnId);
    }

    public SaveGame {
        if (version != CURRENT_VERSION) {
            throw new SaveValidationException("unsupported save version: " + version);
        }
        if (checkpointRoomId == null || checkpointRoomId.isBlank()) {
            throw new SaveValidationException("checkpointRoomId must not be blank");
        }
        if (checkpointSpawnId == null || checkpointSpawnId.isBlank()) {
            throw new SaveValidationException("checkpointSpawnId must not be blank");
        }
    }
}
