package umbra.boss;

public record BossHitboxShape(
        String type,
        String direction,
        int slot
) {
    public BossHitboxShape {
        requireSnakeCase("hitbox shape type", type);
        if (direction == null || direction.isBlank()) {
            direction = "facing";
        } else {
            requireSnakeCase("hitbox shape direction", direction);
        }
        if (slot < 0) {
            throw new BossDefinitionValidationException("hitbox shape slot must not be negative");
        }
    }

    private static void requireSnakeCase(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new BossDefinitionValidationException(field + " must not be blank");
        }
        if (!value.matches("[a-z][a-z0-9_]*")) {
            throw new BossDefinitionValidationException(field + " must be snake_case: " + value);
        }
    }
}
