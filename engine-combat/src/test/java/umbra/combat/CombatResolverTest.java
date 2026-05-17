package umbra.combat;

import org.junit.jupiter.api.Test;
import umbra.physics.Aabb;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CombatResolverTest {
    @Test
    void activeHitboxDamagesOverlappingHurtbox() {
        HitboxInstance hitbox = new HitboxInstance(1, slash(), new Aabb(10, 10, 30, 20), true);
        HurtboxInstance hurtbox = new HurtboxInstance(2, new Aabb(25, 15, 20, 20), true);

        List<DamageEvent> events = new CombatResolver().resolve(List.of(hitbox), List.of(hurtbox));

        assertEquals(1, events.size());
        DamageEvent event = events.get(0);
        assertEquals(1, event.sourceEntityId());
        assertEquals(2, event.targetEntityId());
        assertEquals("player_slash_01", event.attackId());
        assertEquals(1, event.damage());
        assertTrue(event.knockbackX() > 0.0f);
    }

    @Test
    void inactiveHitboxDoesNotDamage() {
        HitboxInstance hitbox = new HitboxInstance(1, slash(), new Aabb(10, 10, 30, 20), false);
        HurtboxInstance hurtbox = new HurtboxInstance(2, new Aabb(25, 15, 20, 20), true);

        List<DamageEvent> events = new CombatResolver().resolve(List.of(hitbox), List.of(hurtbox));

        assertTrue(events.isEmpty());
    }

    @Test
    void disabledHurtboxDoesNotReceiveDamage() {
        HitboxInstance hitbox = new HitboxInstance(1, slash(), new Aabb(10, 10, 30, 20), true);
        HurtboxInstance hurtbox = new HurtboxInstance(2, new Aabb(25, 15, 20, 20), false);

        List<DamageEvent> events = new CombatResolver().resolve(List.of(hitbox), List.of(hurtbox));

        assertTrue(events.isEmpty());
    }

    @Test
    void sameHitboxCannotDamageTargetTwice() {
        HitboxInstance hitbox = new HitboxInstance(1, slash(), new Aabb(10, 10, 30, 20), true);
        HurtboxInstance hurtbox = new HurtboxInstance(2, new Aabb(25, 15, 20, 20), true);
        CombatResolver resolver = new CombatResolver();

        assertEquals(1, resolver.resolve(List.of(hitbox), List.of(hurtbox)).size());
        assertTrue(resolver.resolve(List.of(hitbox), List.of(hurtbox)).isEmpty());
    }

    @Test
    void ownerCannotHitSelf() {
        HitboxInstance hitbox = new HitboxInstance(1, slash(), new Aabb(10, 10, 30, 20), true);
        HurtboxInstance hurtbox = new HurtboxInstance(1, new Aabb(25, 15, 20, 20), true);

        List<DamageEvent> events = new CombatResolver().resolve(List.of(hitbox), List.of(hurtbox));

        assertTrue(events.isEmpty());
    }

    private AttackDefinition slash() {
        return new AttackDefinition("player_slash_01", 1, 160.0f, 40.0f, 0.045f, "slash");
    }
}
