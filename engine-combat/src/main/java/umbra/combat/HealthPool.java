package umbra.combat;

import java.util.Objects;

/**
 * Runtime health model with deterministic invulnerability frames.
 */
public final class HealthPool {
    private final int maxHealth;
    private final float invulnerabilitySecondsOnHit;
    private int currentHealth;
    private float invulnerabilityTimerSeconds;

    public HealthPool(int maxHealth, float invulnerabilitySecondsOnHit) {
        if (maxHealth <= 0) {
            throw new IllegalArgumentException("maxHealth must be positive");
        }
        if (invulnerabilitySecondsOnHit < 0.0f) {
            throw new IllegalArgumentException("invulnerabilitySecondsOnHit must not be negative");
        }
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.invulnerabilitySecondsOnHit = invulnerabilitySecondsOnHit;
    }

    public void update(float deltaSeconds) {
        if (deltaSeconds < 0.0f) {
            throw new IllegalArgumentException("deltaSeconds must not be negative");
        }
        invulnerabilityTimerSeconds = Math.max(0.0f, invulnerabilityTimerSeconds - deltaSeconds);
    }

    public DamageApplication apply(DamageEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        if (defeated()) {
            return DamageApplication.rejected(event, currentHealth, true, DamageRejectionReason.DEFEATED);
        }
        if (invulnerable()) {
            return DamageApplication.rejected(event, currentHealth, false, DamageRejectionReason.INVULNERABLE);
        }

        int previousHealth = currentHealth;
        int damageApplied = Math.min(currentHealth, event.damage());
        currentHealth -= damageApplied;
        if (!defeated()) {
            invulnerabilityTimerSeconds = invulnerabilitySecondsOnHit;
        }
        return DamageApplication.accepted(event, damageApplied, previousHealth, currentHealth);
    }

    public void reset() {
        currentHealth = maxHealth;
        invulnerabilityTimerSeconds = 0.0f;
    }

    public int maxHealth() {
        return maxHealth;
    }

    public int currentHealth() {
        return currentHealth;
    }

    public boolean invulnerable() {
        return invulnerabilityTimerSeconds > 0.0f;
    }

    public float invulnerabilityTimerSeconds() {
        return invulnerabilityTimerSeconds;
    }

    public boolean defeated() {
        return currentHealth == 0;
    }
}
