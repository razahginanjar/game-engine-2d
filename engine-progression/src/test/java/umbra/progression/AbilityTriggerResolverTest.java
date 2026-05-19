package umbra.progression;

import org.junit.jupiter.api.Test;
import umbra.physics.Aabb;
import umbra.room.RoomDefinition;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AbilityTriggerResolverTest {
    private final AbilityTriggerResolver resolver = new AbilityTriggerResolver();

    @Test
    void overlappingPickupUnlocksOnlyWhenAbilityIsMissing() {
        AbilityState state = new AbilityState();
        List<RoomDefinition.AbilityPickupDefinition> pickups = List.of(
                new RoomDefinition.AbilityPickupDefinition("dash_pickup", "dash", 32.0f, 32.0f, 24.0f, 24.0f)
        );

        Optional<String> pickup = resolver.findUnlockablePickup(state, pickups, new Aabb(40.0f, 32.0f, 18.0f, 38.0f));

        assertEquals(Optional.of("dash"), pickup);
        state.unlock("dash");
        assertEquals(Optional.empty(), resolver.findUnlockablePickup(state, pickups, new Aabb(40.0f, 32.0f, 18.0f, 38.0f)));
    }

    @Test
    void lockedGateBlocksAndUnlockedGateDoesNot() {
        AbilityState state = new AbilityState();
        List<RoomDefinition.AbilityGateDefinition> gates = List.of(
                new RoomDefinition.AbilityGateDefinition("dash_gate", "dash", 64.0f, 32.0f, 16.0f, 64.0f)
        );

        Optional<AbilityGateBlock> block = resolver.findBlockingGate(state, gates, new Aabb(60.0f, 32.0f, 18.0f, 38.0f));

        assertTrue(block.isPresent());
        assertEquals("dash_gate", block.get().gate().id());
        state.unlock("dash");
        assertEquals(Optional.empty(), resolver.findBlockingGate(state, gates, new Aabb(60.0f, 32.0f, 18.0f, 38.0f)));
    }

    @Test
    void resolvesActorToNearestHorizontalGateSide() {
        assertEquals(46.0f, resolver.resolveHorizontalBlockX(
                new Aabb(60.0f, 32.0f, 18.0f, 38.0f),
                new Aabb(64.0f, 32.0f, 16.0f, 64.0f)
        ));
        assertEquals(80.0f, resolver.resolveHorizontalBlockX(
                new Aabb(72.0f, 32.0f, 18.0f, 38.0f),
                new Aabb(64.0f, 32.0f, 16.0f, 64.0f)
        ));
    }
}
