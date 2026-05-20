package umbra.boss;

import java.util.List;

public record BossArenaStatus(
        BossFightState state,
        BossPhaseDefinition phase,
        boolean locked,
        List<String> lockedDoorIds
) {
    public BossArenaStatus {
        if (state == null) {
            throw new IllegalArgumentException("state must not be null");
        }
        if (phase == null) {
            throw new IllegalArgumentException("phase must not be null");
        }
        lockedDoorIds = lockedDoorIds == null ? List.of() : List.copyOf(lockedDoorIds);
    }

    public boolean active() {
        return state == BossFightState.ACTIVE;
    }

    public boolean defeated() {
        return state == BossFightState.DEFEATED;
    }
}
