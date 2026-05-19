package umbra.progression;

import umbra.physics.Aabb;
import umbra.room.RoomDefinition;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class AbilityTriggerResolver {
    public Optional<String> findUnlockablePickup(
            AbilityState abilityState,
            List<RoomDefinition.AbilityPickupDefinition> pickups,
            Aabb actorBounds
    ) {
        Objects.requireNonNull(abilityState, "abilityState must not be null");
        Objects.requireNonNull(pickups, "pickups must not be null");
        Objects.requireNonNull(actorBounds, "actorBounds must not be null");
        for (RoomDefinition.AbilityPickupDefinition pickup : pickups) {
            Aabb pickupBounds = new Aabb(pickup.x(), pickup.y(), pickup.width(), pickup.height());
            if (!abilityState.has(pickup.abilityId()) && actorBounds.overlaps(pickupBounds)) {
                return Optional.of(pickup.abilityId());
            }
        }
        return Optional.empty();
    }

    public Optional<AbilityGateBlock> findBlockingGate(
            AbilityState abilityState,
            List<RoomDefinition.AbilityGateDefinition> gates,
            Aabb actorBounds
    ) {
        Objects.requireNonNull(abilityState, "abilityState must not be null");
        Objects.requireNonNull(gates, "gates must not be null");
        Objects.requireNonNull(actorBounds, "actorBounds must not be null");
        for (RoomDefinition.AbilityGateDefinition gate : gates) {
            Aabb gateBounds = new Aabb(gate.x(), gate.y(), gate.width(), gate.height());
            if (!abilityState.has(gate.requiredAbilityId()) && actorBounds.overlaps(gateBounds)) {
                return Optional.of(new AbilityGateBlock(gate, gateBounds));
            }
        }
        return Optional.empty();
    }

    public float resolveHorizontalBlockX(Aabb actorBounds, Aabb gateBounds) {
        Objects.requireNonNull(actorBounds, "actorBounds must not be null");
        Objects.requireNonNull(gateBounds, "gateBounds must not be null");
        float actorCenterX = actorBounds.x() + actorBounds.width() * 0.5f;
        float gateCenterX = gateBounds.x() + gateBounds.width() * 0.5f;
        return actorCenterX < gateCenterX
                ? gateBounds.x() - actorBounds.width()
                : gateBounds.right();
    }
}
