package umbra.combat;

import umbra.physics.Aabb;

import java.util.Objects;

/**
 * Data-driven hitbox shape anchored to an owner's bounds.
 */
public record HitboxDefinition(
        float width,
        float height,
        float forwardOffset,
        float verticalOffset
) {
    public HitboxDefinition {
        if (width <= 0.0f || height <= 0.0f) {
            throw new IllegalArgumentException("hitbox size must be positive");
        }
        if (forwardOffset < 0.0f) {
            throw new IllegalArgumentException("forwardOffset must not be negative");
        }
    }

    public Aabb createBounds(Aabb ownerBounds, FacingDirection facing) {
        Objects.requireNonNull(ownerBounds, "ownerBounds must not be null");
        Objects.requireNonNull(facing, "facing must not be null");

        float x = facing == FacingDirection.RIGHT
                ? ownerBounds.right() + forwardOffset
                : ownerBounds.x() - forwardOffset - width;
        return new Aabb(x, ownerBounds.y() + verticalOffset, width, height);
    }

    public HitboxInstance createInstance(
            int ownerEntityId,
            CombatTeam team,
            AttackDefinition attack,
            Aabb ownerBounds,
            FacingDirection facing,
            boolean active
    ) {
        return new HitboxInstance(ownerEntityId, team, attack, createBounds(ownerBounds, facing), active);
    }
}
