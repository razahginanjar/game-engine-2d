package umbra.room;

public record RoomTransitionRequest(
        String targetRoomId,
        String targetSpawnId
) {
    public RoomTransitionRequest {
        if (targetRoomId == null || targetRoomId.isBlank()) {
            throw new IllegalArgumentException("targetRoomId must not be blank");
        }
        if (targetSpawnId == null || targetSpawnId.isBlank()) {
            throw new IllegalArgumentException("targetSpawnId must not be blank");
        }
    }
}
