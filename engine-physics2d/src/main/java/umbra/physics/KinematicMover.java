package umbra.physics;

/**
 * Moves a body one axis at a time against a tile grid. Small substeps make
 * high-speed movement deterministic without introducing a rigid-body solver.
 */
public final class KinematicMover {
    public MovementResult move(KinematicBody body, CollisionGrid grid, float dx, float dy) {
        AxisResult horizontal = moveAxis(body, grid, dx, true);
        AxisResult vertical = moveAxis(body, grid, dy, false);
        return new MovementResult(
                horizontal.negativeHit,
                horizontal.positiveHit,
                vertical.negativeHit,
                vertical.positiveHit
        );
    }

    private AxisResult moveAxis(KinematicBody body, CollisionGrid grid, float amount, boolean horizontal) {
        if (amount == 0.0f) {
            return new AxisResult(false, false);
        }

        float remaining = amount;
        float sign = Math.signum(amount);
        boolean negativeHit = false;
        boolean positiveHit = false;
        float maxStep = Math.max(1.0f, grid.tileSize() / 4.0f);

        while (Math.abs(remaining) > 0.0001f) {
            float step = sign * Math.min(Math.abs(remaining), maxStep);
            Aabb candidate = horizontal
                    ? body.bounds().translated(step, 0.0f)
                    : body.bounds().translated(0.0f, step);

            if (grid.collides(candidate)) {
                if (sign < 0.0f) {
                    negativeHit = true;
                } else {
                    positiveHit = true;
                }
                break;
            }

            if (horizontal) {
                body.translate(step, 0.0f);
            } else {
                body.translate(0.0f, step);
            }
            remaining -= step;
        }

        return new AxisResult(negativeHit, positiveHit);
    }

    private record AxisResult(boolean negativeHit, boolean positiveHit) {
    }
}
