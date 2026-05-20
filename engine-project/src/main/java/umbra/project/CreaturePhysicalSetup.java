package umbra.project;

public record CreaturePhysicalSetup(
        float bodyWidth,
        float bodyHeight,
        float hurtboxWidth,
        float hurtboxHeight,
        float movementSpeed,
        boolean flying
) {
    public CreaturePhysicalSetup {
        requirePositive("body_width", bodyWidth);
        requirePositive("body_height", bodyHeight);
        requirePositive("hurtbox_width", hurtboxWidth);
        requirePositive("hurtbox_height", hurtboxHeight);
        requirePositive("movement_speed", movementSpeed);
    }

    private static void requirePositive(String field, float value) {
        if (value <= 0.0f) {
            throw new CreatureDefinitionValidationException(field + " must be positive");
        }
    }
}
