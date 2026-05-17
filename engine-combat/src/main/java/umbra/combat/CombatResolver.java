package umbra.combat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Resolves active hitboxes against enabled hurtboxes and emits deterministic
 * damage events. The resolver does not mutate health; DamageSystem will consume
 * these events later.
 */
public final class CombatResolver {
    public List<DamageEvent> resolve(Collection<HitboxInstance> hitboxes, Collection<HurtboxInstance> hurtboxes) {
        List<DamageEvent> events = new ArrayList<>();
        for (HitboxInstance hitbox : hitboxes) {
            if (!hitbox.active()) {
                continue;
            }
            for (HurtboxInstance hurtbox : hurtboxes) {
                if (!hurtbox.enabled()
                        || hitbox.ownerEntityId() == hurtbox.ownerEntityId()
                        || sameNonNeutralTeam(hitbox, hurtbox)
                        || hitbox.hasAlreadyHit(hurtbox.ownerEntityId())
                        || !hitbox.bounds().overlaps(hurtbox.bounds())) {
                    continue;
                }
                hitbox.markHit(hurtbox.ownerEntityId());
                AttackDefinition attack = hitbox.attack();
                events.add(new DamageEvent(
                        hitbox.ownerEntityId(),
                        hurtbox.ownerEntityId(),
                        attack.id(),
                        attack.damage(),
                        signedKnockbackX(hitbox, hurtbox),
                        attack.knockbackY(),
                        attack.hitPauseSeconds(),
                        attack.damageType()
                ));
            }
        }
        return events;
    }

    private float signedKnockbackX(HitboxInstance hitbox, HurtboxInstance hurtbox) {
        float hitboxCenter = hitbox.bounds().x() + hitbox.bounds().width() * 0.5f;
        float hurtboxCenter = hurtbox.bounds().x() + hurtbox.bounds().width() * 0.5f;
        float sign = hurtboxCenter >= hitboxCenter ? 1.0f : -1.0f;
        return hitbox.attack().knockbackX() * sign;
    }

    private boolean sameNonNeutralTeam(HitboxInstance hitbox, HurtboxInstance hurtbox) {
        return hitbox.team() != CombatTeam.NEUTRAL && hitbox.team() == hurtbox.team();
    }
}
