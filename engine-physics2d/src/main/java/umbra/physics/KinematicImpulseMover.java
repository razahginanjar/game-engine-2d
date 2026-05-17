package umbra.physics;

import java.util.Objects;

/**
 * Applies an existing velocity impulse through gravity and tile collision.
 */
public final class KinematicImpulseMover {
    private final KinematicMover mover = new KinematicMover();

    public MovementResult update(
            KinematicBody body,
            CollisionGrid grid,
            KinematicImpulseConfig config,
            float deltaSeconds
    ) {
        Objects.requireNonNull(body, "body must not be null");
        Objects.requireNonNull(grid, "grid must not be null");
        Objects.requireNonNull(config, "config must not be null");
        if (deltaSeconds < 0.0f) {
            throw new IllegalArgumentException("deltaSeconds must not be negative");
        }

        float nextY = body.velocityY() - config.gravityPixelsPerSecondSquared() * deltaSeconds;
        body.setVelocityY(Math.max(nextY, -config.maxFallSpeedPixelsPerSecond()));

        MovementResult result = mover.move(
                body,
                grid,
                body.velocityX() * deltaSeconds,
                body.velocityY() * deltaSeconds
        );

        if ((result.hitLeft() && body.velocityX() < 0.0f) || (result.hitRight() && body.velocityX() > 0.0f)) {
            body.setVelocityX(0.0f);
        }
        if (result.hitGround() && body.velocityY() < 0.0f) {
            body.setVelocityY(0.0f);
        } else if (result.hitCeiling() && body.velocityY() > 0.0f) {
            body.setVelocityY(0.0f);
        }

        return result;
    }
}
