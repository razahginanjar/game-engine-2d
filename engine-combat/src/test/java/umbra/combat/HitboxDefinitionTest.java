package umbra.combat;

import org.junit.jupiter.api.Test;
import umbra.physics.Aabb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HitboxDefinitionTest {
    @Test
    void createsBoundsInFrontOfRightFacingOwner() {
        HitboxDefinition definition = new HitboxDefinition(34.0f, 28.0f, 2.0f, 5.0f);

        Aabb bounds = definition.createBounds(new Aabb(100.0f, 50.0f, 18.0f, 38.0f), FacingDirection.RIGHT);

        assertEquals(new Aabb(120.0f, 55.0f, 34.0f, 28.0f), bounds);
    }

    @Test
    void createsBoundsInFrontOfLeftFacingOwner() {
        HitboxDefinition definition = new HitboxDefinition(34.0f, 28.0f, 2.0f, 5.0f);

        Aabb bounds = definition.createBounds(new Aabb(100.0f, 50.0f, 18.0f, 38.0f), FacingDirection.LEFT);

        assertEquals(new Aabb(64.0f, 55.0f, 34.0f, 28.0f), bounds);
    }

    @Test
    void createInstanceUsesTeamAttackAndBounds() {
        HitboxDefinition definition = new HitboxDefinition(10.0f, 8.0f, 0.0f, 1.0f);
        AttackDefinition attack = new AttackDefinition("test", 1, 0.0f, 0.0f, 0.0f, "test");

        HitboxInstance instance = definition.createInstance(
                7,
                CombatTeam.PLAYER,
                attack,
                new Aabb(20.0f, 30.0f, 10.0f, 20.0f),
                FacingDirection.RIGHT,
                true
        );

        assertEquals(7, instance.ownerEntityId());
        assertEquals(CombatTeam.PLAYER, instance.team());
        assertEquals(attack, instance.attack());
        assertEquals(new Aabb(30.0f, 31.0f, 10.0f, 8.0f), instance.bounds());
        assertTrue(instance.active());
    }

    @Test
    void hitboxInstanceCanMoveWithoutResettingAlreadyHitTargets() {
        AttackDefinition attack = new AttackDefinition("test", 1, 0.0f, 0.0f, 0.0f, "test");
        HitboxInstance instance = new HitboxInstance(
                1,
                CombatTeam.PLAYER,
                attack,
                new Aabb(0.0f, 0.0f, 10.0f, 10.0f),
                true
        );

        instance.markHit(2);
        instance.setBounds(new Aabb(20.0f, 0.0f, 10.0f, 10.0f));

        assertTrue(instance.hasAlreadyHit(2));
        assertEquals(new Aabb(20.0f, 0.0f, 10.0f, 10.0f), instance.bounds());
    }

    @Test
    void rejectsInvalidDefinitions() {
        assertThrows(IllegalArgumentException.class, () -> new HitboxDefinition(0.0f, 1.0f, 0.0f, 0.0f));
        assertThrows(IllegalArgumentException.class, () -> new HitboxDefinition(1.0f, 0.0f, 0.0f, 0.0f));
        assertThrows(IllegalArgumentException.class, () -> new HitboxDefinition(1.0f, 1.0f, -0.1f, 0.0f));
    }
}
