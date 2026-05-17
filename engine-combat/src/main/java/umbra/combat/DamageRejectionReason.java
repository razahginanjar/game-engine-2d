package umbra.combat;

/**
 * Explains why a damage event was ignored by a health pool.
 */
public enum DamageRejectionReason {
    NONE,
    INVULNERABLE,
    DEFEATED
}
