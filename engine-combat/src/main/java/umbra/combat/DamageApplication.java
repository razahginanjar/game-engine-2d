package umbra.combat;

/**
 * Result of attempting to apply a damage event to health.
 */
public record DamageApplication(
        DamageEvent event,
        boolean applied,
        int damageApplied,
        int previousHealth,
        int remainingHealth,
        boolean defeated,
        DamageRejectionReason rejectionReason
) {
    static DamageApplication accepted(DamageEvent event, int damageApplied, int previousHealth, int remainingHealth) {
        return new DamageApplication(
                event,
                true,
                damageApplied,
                previousHealth,
                remainingHealth,
                remainingHealth == 0,
                DamageRejectionReason.NONE
        );
    }

    static DamageApplication rejected(
            DamageEvent event,
            int currentHealth,
            boolean defeated,
            DamageRejectionReason rejectionReason
    ) {
        return new DamageApplication(event, false, 0, currentHealth, currentHealth, defeated, rejectionReason);
    }
}
