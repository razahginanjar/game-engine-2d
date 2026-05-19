package umbra.save;

import java.util.List;

public record SaveGame(
        int version,
        String checkpointRoomId,
        String checkpointSpawnId,
        List<String> visitedRoomIds
) {
    public static final int CURRENT_VERSION = 1;

    public SaveGame(String checkpointRoomId, String checkpointSpawnId) {
        this(CURRENT_VERSION, checkpointRoomId, checkpointSpawnId, List.of(checkpointRoomId));
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
        visitedRoomIds = visitedRoomIds == null ? List.of() : List.copyOf(visitedRoomIds);
        for (String roomId : visitedRoomIds) {
            if (roomId == null || roomId.isBlank()) {
                throw new SaveValidationException("visitedRoomIds must not contain blank values");
            }
        }
    }
}
