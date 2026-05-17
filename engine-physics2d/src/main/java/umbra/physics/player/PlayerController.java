package umbra.physics.player;

import umbra.physics.CollisionGrid;
import umbra.physics.KinematicBody;
import umbra.physics.KinematicMover;
import umbra.physics.MovementResult;

/**
 * Deterministic kinematic platformer controller. It owns player movement
 * timing rules such as coyote time and jump buffering.
 */
public final class PlayerController {
    private final PlayerControllerConfig config;
    private final KinematicMover mover;
    private float coyoteTimer;
    private float jumpBufferTimer;
    private boolean grounded;

    public PlayerController(PlayerControllerConfig config) {
        this.config = config;
        this.mover = new KinematicMover();
    }

    public PlayerState update(PlayerInput input, KinematicBody body, CollisionGrid grid, float deltaSeconds) {
        if (input.jumpPressed()) {
            jumpBufferTimer = config.jumpBufferSeconds();
        } else {
            jumpBufferTimer = Math.max(0.0f, jumpBufferTimer - deltaSeconds);
        }

        grounded = isGrounded(body, grid);
        if (grounded) {
            coyoteTimer = config.coyoteTimeSeconds();
        } else {
            coyoteTimer = Math.max(0.0f, coyoteTimer - deltaSeconds);
        }

        updateHorizontal(input, body, deltaSeconds);
        tryStartJump(body);
        cutJumpWhenReleased(input, body);
        applyGravity(body, deltaSeconds);

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
            grounded = true;
            coyoteTimer = config.coyoteTimeSeconds();
            tryStartJump(body);
        } else if (result.hitCeiling() && body.velocityY() > 0.0f) {
            body.setVelocityY(0.0f);
        } else {
            grounded = isGrounded(body, grid);
        }

        if (grounded && Math.abs(body.velocityX()) < 0.01f) {
            return PlayerState.IDLE;
        }
        if (grounded) {
            return PlayerState.RUN;
        }
        return body.velocityY() > 0.0f ? PlayerState.JUMP : PlayerState.FALL;
    }

    public boolean grounded() {
        return grounded;
    }

    private void updateHorizontal(PlayerInput input, KinematicBody body, float deltaSeconds) {
        int direction = 0;
        if (input.left()) {
            direction--;
        }
        if (input.right()) {
            direction++;
        }

        float targetSpeed = direction * config.moveMaxSpeedPixelsPerSecond();
        float acceleration = grounded
                ? (direction == 0 ? config.groundDecelerationPixelsPerSecondSquared() : config.groundAccelerationPixelsPerSecondSquared())
                : config.airAccelerationPixelsPerSecondSquared();
        body.setVelocityX(moveToward(body.velocityX(), targetSpeed, acceleration * deltaSeconds));
    }

    private void tryStartJump(KinematicBody body) {
        if (jumpBufferTimer > 0.0f && coyoteTimer > 0.0f) {
            body.setVelocityY(config.jumpSpeedPixelsPerSecond());
            jumpBufferTimer = 0.0f;
            coyoteTimer = 0.0f;
            grounded = false;
        }
    }

    private void cutJumpWhenReleased(PlayerInput input, KinematicBody body) {
        if (!input.jumpHeld() && body.velocityY() > 0.0f) {
            body.setVelocityY(body.velocityY() * config.jumpCutMultiplier());
        }
    }

    private void applyGravity(KinematicBody body, float deltaSeconds) {
        float nextY = body.velocityY() - config.gravityPixelsPerSecondSquared() * deltaSeconds;
        body.setVelocityY(Math.max(nextY, -config.maxFallSpeedPixelsPerSecond()));
    }

    private boolean isGrounded(KinematicBody body, CollisionGrid grid) {
        return grid.collides(body.bounds().translated(0.0f, -0.5f));
    }

    private float moveToward(float current, float target, float maxDelta) {
        if (current < target) {
            return Math.min(current + maxDelta, target);
        }
        if (current > target) {
            return Math.max(current - maxDelta, target);
        }
        return target;
    }
}
