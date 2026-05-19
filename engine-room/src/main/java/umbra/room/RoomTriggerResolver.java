package umbra.room;

import java.util.Objects;
import java.util.Optional;

public final class RoomTriggerResolver {
    public Optional<RoomTransitionRequest> findDoorTransition(
            RoomDefinition room,
            String currentRoomId,
            float actorX,
            float actorY,
            float actorWidth,
            float actorHeight
    ) {
        Objects.requireNonNull(room, "room must not be null");
        if (currentRoomId == null || currentRoomId.isBlank()) {
            throw new IllegalArgumentException("currentRoomId must not be blank");
        }
        for (RoomDefinition.DoorDefinition door : room.doors()) {
            if (intersects(actorX, actorY, actorWidth, actorHeight, door.x(), door.y(), door.width(), door.height())) {
                String targetRoomId = door.targetRoom().equals("self") ? currentRoomId : door.targetRoom();
                return Optional.of(new RoomTransitionRequest(targetRoomId, door.targetSpawn()));
            }
        }
        return Optional.empty();
    }

    public Optional<String> findCheckpoint(
            RoomDefinition room,
            float actorX,
            float actorY,
            float actorWidth,
            float actorHeight,
            float checkpointWidth,
            float checkpointHeight
    ) {
        Objects.requireNonNull(room, "room must not be null");
        if (checkpointWidth <= 0.0f || checkpointHeight <= 0.0f) {
            throw new IllegalArgumentException("checkpoint trigger size must be positive");
        }
        for (RoomDefinition.SpawnPoint spawn : room.spawns()) {
            if (!spawn.type().equals("checkpoint")) {
                continue;
            }
            float checkpointX = spawn.x() - checkpointWidth * 0.5f;
            if (intersects(actorX, actorY, actorWidth, actorHeight, checkpointX, spawn.y(), checkpointWidth, checkpointHeight)) {
                return Optional.of(spawn.id());
            }
        }
        return Optional.empty();
    }

    private boolean intersects(
            float firstX,
            float firstY,
            float firstWidth,
            float firstHeight,
            float secondX,
            float secondY,
            float secondWidth,
            float secondHeight
    ) {
        return firstX < secondX + secondWidth
                && firstX + firstWidth > secondX
                && firstY < secondY + secondHeight
                && firstY + firstHeight > secondY;
    }
}
