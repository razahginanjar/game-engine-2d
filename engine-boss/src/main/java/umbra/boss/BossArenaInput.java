package umbra.boss;

import umbra.physics.Aabb;

import java.util.Objects;

public record BossArenaInput(
        Aabb playerBounds,
        int bossCurrentHealth,
        int bossMaxHealth,
        boolean defeatFlagSet
) {
    public BossArenaInput {
        Objects.requireNonNull(playerBounds, "playerBounds must not be null");
        if (bossMaxHealth <= 0) {
            throw new IllegalArgumentException("bossMaxHealth must be positive");
        }
    }
}
