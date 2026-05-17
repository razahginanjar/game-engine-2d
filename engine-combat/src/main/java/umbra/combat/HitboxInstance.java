package umbra.combat;

import umbra.physics.Aabb;

import java.util.HashSet;
import java.util.Set;

/**
 * Runtime hitbox instance for an active attack window.
 */
public final class HitboxInstance {
    private final int ownerEntityId;
    private final AttackDefinition attack;
    private final Aabb bounds;
    private final Set<Integer> alreadyHitEntityIds = new HashSet<>();
    private boolean active;

    public HitboxInstance(int ownerEntityId, AttackDefinition attack, Aabb bounds, boolean active) {
        this.ownerEntityId = ownerEntityId;
        this.attack = attack;
        this.bounds = bounds;
        this.active = active;
    }

    public int ownerEntityId() {
        return ownerEntityId;
    }

    public AttackDefinition attack() {
        return attack;
    }

    public Aabb bounds() {
        return bounds;
    }

    public boolean active() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean hasAlreadyHit(int entityId) {
        return alreadyHitEntityIds.contains(entityId);
    }

    public void markHit(int entityId) {
        alreadyHitEntityIds.add(entityId);
    }

    public void resetAlreadyHit() {
        alreadyHitEntityIds.clear();
    }
}
