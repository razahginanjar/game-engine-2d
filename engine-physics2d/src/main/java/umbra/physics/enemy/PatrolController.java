package umbra.physics.enemy;

import umbra.physics.Aabb;
import umbra.physics.CollisionGrid;
import umbra.physics.KinematicBody;
import umbra.physics.KinematicMover;
import umbra.physics.MovementResult;

/**
 * Deterministic one-dimensional patrol movement for ground enemies.
 */
public final class PatrolController {
    private final PatrolControllerConfig config;
    private final KinematicMover mover = new KinematicMover();
    private int direction;

    public PatrolController(PatrolControllerConfig config, int initialDirection) {
        this.config = config;
        this.direction = normalizeDirection(initialDirection);
    }

    public void update(KinematicBody body, CollisionGrid grid, float deltaSeconds) {
        if (deltaSeconds < 0.0f) {
            throw new IllegalArgumentException("deltaSeconds must not be negative");
        }

        boolean grounded = grounded(body, grid);
        if (grounded && !hasGroundAhead(body, grid)) {
            reverse();
        }

        body.setVelocityX(direction * config.speedPixelsPerSecond());
        applyGravity(body, deltaSeconds);

        MovementResult result = mover.move(
                body,
                grid,
                body.velocityX() * deltaSeconds,
                body.velocityY() * deltaSeconds
        );

        if ((result.hitLeft() && direction < 0) || (result.hitRight() && direction > 0)) {
            reverse();
            body.setVelocityX(0.0f);
        }
        if (result.hitGround() && body.velocityY() < 0.0f) {
            body.setVelocityY(0.0f);
        } else if (result.hitCeiling() && body.velocityY() > 0.0f) {
            body.setVelocityY(0.0f);
        }
    }

    public void reset(int initialDirection) {
        direction = normalizeDirection(initialDirection);
    }

    public int direction() {
        return direction;
    }

    public boolean facingRight() {
        return direction > 0;
    }

    private void applyGravity(KinematicBody body, float deltaSeconds) {
        float nextY = body.velocityY() - config.gravityPixelsPerSecondSquared() * deltaSeconds;
        body.setVelocityY(Math.max(nextY, -config.maxFallSpeedPixelsPerSecond()));
    }

    private boolean grounded(KinematicBody body, CollisionGrid grid) {
        return grid.collides(body.bounds().translated(0.0f, -0.5f));
    }

    private boolean hasGroundAhead(KinematicBody body, CollisionGrid grid) {
        float probeX = direction > 0 ? body.x() + body.width() + 1.0f : body.x() - 2.0f;
        Aabb probe = new Aabb(probeX, body.y() - 2.0f, 2.0f, 2.0f);
        return grid.collides(probe);
    }

    private void reverse() {
        direction *= -1;
    }

    private int normalizeDirection(int direction) {
        return direction < 0 ? -1 : 1;
    }
}
