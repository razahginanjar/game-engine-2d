package umbra.combat;

import umbra.physics.Aabb;

/**
 * Runtime damage receiver. Invulnerability and death states can disable the
 * hurtbox without removing the entity.
 */
public record HurtboxInstance(int ownerEntityId, Aabb bounds, boolean enabled) {
}
