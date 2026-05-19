package umbra.progression;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Runtime progression state for abilities unlocked by the player.
 */
public final class AbilityState {
    private final Set<String> unlockedAbilityIds = new LinkedHashSet<>();

    public boolean unlock(String abilityId) {
        validateAbilityId(abilityId);
        return unlockedAbilityIds.add(abilityId);
    }

    public void unlockAll(List<String> abilityIds) {
        Objects.requireNonNull(abilityIds, "abilityIds must not be null");
        for (String abilityId : abilityIds) {
            unlock(abilityId);
        }
    }

    public boolean has(String abilityId) {
        validateAbilityId(abilityId);
        return unlockedAbilityIds.contains(abilityId);
    }

    public List<String> unlockedAbilityIds() {
        return List.copyOf(unlockedAbilityIds);
    }

    public void clear() {
        unlockedAbilityIds.clear();
    }

    private void validateAbilityId(String abilityId) {
        if (abilityId == null || abilityId.isBlank()) {
            throw new IllegalArgumentException("abilityId must not be blank");
        }
    }
}
