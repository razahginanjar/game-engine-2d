package umbra.boss;

import java.util.List;
import java.util.Objects;

/**
 * Engine-level boss arena lock controller. It activates when the player enters
 * the arena trigger, locks configured room exits while the fight is active, and
 * unlocks the arena once the boss is defeated or a persisted defeat flag exists.
 */
public final class BossArenaController {
    private final BossArenaDefinition definition;
    private final BossPhaseResolver phaseResolver;
    private BossFightState state = BossFightState.DORMANT;

    public BossArenaController(BossArenaDefinition definition, List<BossPhaseDefinition> phases) {
        this.definition = Objects.requireNonNull(definition, "definition must not be null");
        this.phaseResolver = new BossPhaseResolver(phases);
    }

    public BossArenaStatus update(BossArenaInput input) {
        Objects.requireNonNull(input, "input must not be null");
        BossPhaseDefinition phase = phaseResolver.resolve(input.bossCurrentHealth(), input.bossMaxHealth());
        if (input.defeatFlagSet() || input.bossCurrentHealth() <= 0) {
            state = BossFightState.DEFEATED;
            return new BossArenaStatus(state, phase, false, List.of());
        }
        if (state == BossFightState.DORMANT && definition.activationBounds().overlaps(input.playerBounds())) {
            state = BossFightState.ACTIVE;
        }
        boolean locked = state == BossFightState.ACTIVE;
        return new BossArenaStatus(state, phase, locked, locked ? definition.lockedDoorIds() : List.of());
    }

    public BossArenaDefinition definition() {
        return definition;
    }

    public BossFightState state() {
        return state;
    }
}
