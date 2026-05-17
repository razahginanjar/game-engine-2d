package umbra.combat;

import umbra.physics.Aabb;

import java.util.Objects;

/**
 * Runtime damage receiver. Invulnerability and death states can disable the
 * hurtbox without removing the entity.
 */
public record HurtboxInstance(int ownerEntityId, CombatTeam team, Aabb bounds, boolean enabled) {
    public HurtboxInstance(int ownerEntityId, Aabb bounds, boolean enabled) {
        this(ownerEntityId, CombatTeam.NEUTRAL, bounds, enabled);
    }

    public HurtboxInstance {
        if (team == null) {
            team = CombatTeam.NEUTRAL;
        }
        Objects.requireNonNull(bounds, "bounds must not be null");
    }
}
