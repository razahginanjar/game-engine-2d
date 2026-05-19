package umbra.progression;

import umbra.physics.Aabb;
import umbra.room.RoomDefinition;

public record AbilityGateBlock(
        RoomDefinition.AbilityGateDefinition gate,
        Aabb gateBounds
) {
}
