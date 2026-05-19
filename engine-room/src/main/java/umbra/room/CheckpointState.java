package umbra.room;

public final class CheckpointState {
    private String roomId;
    private String spawnId;

    public CheckpointState(String roomId, String spawnId) {
        activate(roomId, spawnId);
    }

    public void activate(String roomId, String spawnId) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("roomId must not be blank");
        }
        if (spawnId == null || spawnId.isBlank()) {
            throw new IllegalArgumentException("spawnId must not be blank");
        }
        this.roomId = roomId;
        this.spawnId = spawnId;
    }

    public String roomId() {
        return roomId;
    }

    public String spawnId() {
        return spawnId;
    }

    public RoomTransitionRequest respawnRequest() {
        return new RoomTransitionRequest(roomId, spawnId);
    }
}
