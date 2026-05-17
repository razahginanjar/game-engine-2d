package umbra.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HealthPoolTest {
    @Test
    void damageReducesHealthAndStartsInvulnerability() {
        HealthPool health = new HealthPool(3, 0.5f);

        DamageApplication result = health.apply(hit(1));

        assertTrue(result.applied());
        assertEquals(1, result.damageApplied());
        assertEquals(3, result.previousHealth());
        assertEquals(2, result.remainingHealth());
        assertEquals(2, health.currentHealth());
        assertTrue(health.invulnerable());
    }

    @Test
    void invulnerabilityRejectsRepeatedDamage() {
        HealthPool health = new HealthPool(3, 0.5f);

        assertTrue(health.apply(hit(1)).applied());
        DamageApplication rejected = health.apply(hit(1));

        assertFalse(rejected.applied());
        assertEquals(DamageRejectionReason.INVULNERABLE, rejected.rejectionReason());
        assertEquals(2, health.currentHealth());
    }

    @Test
    void updateExpiresInvulnerability() {
        HealthPool health = new HealthPool(3, 0.5f);

        health.apply(hit(1));
        health.update(0.5f);
        DamageApplication secondHit = health.apply(hit(1));

        assertTrue(secondHit.applied());
        assertEquals(1, health.currentHealth());
    }

    @Test
    void defeatedHealthRejectsMoreDamage() {
        HealthPool health = new HealthPool(2, 0.5f);

        DamageApplication lethal = health.apply(hit(3));
        DamageApplication rejected = health.apply(hit(1));

        assertTrue(lethal.defeated());
        assertEquals(2, lethal.damageApplied());
        assertFalse(rejected.applied());
        assertEquals(DamageRejectionReason.DEFEATED, rejected.rejectionReason());
    }

    @Test
    void resetRestoresHealthAndClearsInvulnerability() {
        HealthPool health = new HealthPool(3, 0.5f);
        health.apply(hit(1));

        health.reset();

        assertEquals(3, health.currentHealth());
        assertFalse(health.invulnerable());
    }

    @Test
    void rejectsInvalidConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new HealthPool(0, 0.1f));
        assertThrows(IllegalArgumentException.class, () -> new HealthPool(1, -0.1f));
    }

    private DamageEvent hit(int damage) {
        return new DamageEvent(10, 20, "test_hit", damage, 0.0f, 0.0f, 0.0f, "test");
    }
}
