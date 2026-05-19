package umbra.room;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class RoomRegistry {
    private final Map<String, RoomDefinition> roomsById = new HashMap<>();

    public RoomRegistry(List<RoomDefinition> rooms) {
        Objects.requireNonNull(rooms, "rooms must not be null");
        if (rooms.isEmpty()) {
            throw new RoomValidationException("room registry must contain at least one room");
        }
        for (RoomDefinition room : rooms) {
            RoomDefinition previous = roomsById.put(room.roomId(), room);
            if (previous != null) {
                throw new RoomValidationException("duplicate room id: " + room.roomId());
            }
        }
        validateDoorTargets();
    }

    public RoomDefinition room(String roomId) {
        RoomDefinition room = roomsById.get(roomId);
        if (room == null) {
            throw new RoomValidationException("unknown room id: " + roomId);
        }
        return room;
    }

    public boolean hasRoom(String roomId) {
        return roomsById.containsKey(roomId);
    }

    private void validateDoorTargets() {
        for (RoomDefinition room : roomsById.values()) {
            for (RoomDefinition.DoorDefinition door : room.doors()) {
                RoomDefinition targetRoom = door.targetRoom().equals("self")
                        ? room
                        : room(door.targetRoom());
                if (!hasSpawn(targetRoom, door.targetSpawn())) {
                    throw new RoomValidationException(
                            "door " + room.roomId() + ":" + door.id()
                                    + " targets missing spawn " + targetRoom.roomId() + ":" + door.targetSpawn()
                    );
                }
            }
        }
    }

    private boolean hasSpawn(RoomDefinition room, String spawnId) {
        for (RoomDefinition.SpawnPoint spawn : room.spawns()) {
            if (spawn.id().equals(spawnId)) {
                return true;
            }
        }
        return false;
    }
}
