package umbra.combat;

import umbra.physics.Aabb;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Runtime hitbox instance for an active attack window.
 */
public final class HitboxInstance {
    private final int ownerEntityId;
    private final CombatTeam team;
    private final AttackDefinition attack;
    private Aabb bounds;
    private final Set<Integer> alreadyHitEntityIds = new HashSet<>();
    private boolean active;

    public HitboxInstance(int ownerEntityId, AttackDefinition attack, Aabb bounds, boolean active) {
        this(ownerEntityId, CombatTeam.NEUTRAL, attack, bounds, active);
    }

    public HitboxInstance(int ownerEntityId, CombatTeam team, AttackDefinition attack, Aabb bounds, boolean active) {
        this.ownerEntityId = ownerEntityId;
        this.team = team == null ? CombatTeam.NEUTRAL : team;
        this.attack = Objects.requireNonNull(attack, "attack must not be null");
        this.bounds = Objects.requireNonNull(bounds, "bounds must not be null");
        this.active = active;
    }

    public int ownerEntityId() {
        return ownerEntityId;
    }

    public CombatTeam team() {
        return team;
    }

    public AttackDefinition attack() {
        return attack;
    }

    public Aabb bounds() {
        return bounds;
    }

    public void setBounds(Aabb bounds) {
        this.bounds = Objects.requireNonNull(bounds, "bounds must not be null");
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
