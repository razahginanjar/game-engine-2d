package umbra.boss;

import java.util.List;

public record BossHitboxWindow(
        int startFrame,
        int endFrame,
        List<BossHitboxShape> shapes
) {
    public BossHitboxWindow {
        if (startFrame <= 0 || endFrame < startFrame) {
            throw new BossDefinitionValidationException("invalid hitbox frame window");
        }
        shapes = shapes == null ? List.of() : List.copyOf(shapes);
        if (shapes.isEmpty()) {
            throw new BossDefinitionValidationException("hitbox window must define at least one shape");
        }
    }

    public boolean containsFrame(int oneBasedFrame) {
        return oneBasedFrame >= startFrame && oneBasedFrame <= endFrame;
    }
}
