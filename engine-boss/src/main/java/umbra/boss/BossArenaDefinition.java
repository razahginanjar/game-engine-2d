package umbra.boss;

import umbra.physics.Aabb;

import java.util.List;
import java.util.Objects;

public record BossArenaDefinition(
        String id,
        String bossId,
        String defeatFlagId,
        Aabb arenaBounds,
        Aabb activationBounds,
        List<String> lockedDoorIds
) {
    public BossArenaDefinition {
        requireId("arena id", id);
        requireId("boss id", bossId);
        requireId("defeat flag id", defeatFlagId);
        Objects.requireNonNull(arenaBounds, "arenaBounds must not be null");
        Objects.requireNonNull(activationBounds, "activationBounds must not be null");
        lockedDoorIds = lockedDoorIds == null ? List.of() : List.copyOf(lockedDoorIds);
        for (String doorId : lockedDoorIds) {
            requireId("locked door id", doorId);
        }
    }

    private static void requireId(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
