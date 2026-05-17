package umbra.physics;

/**
 * Collision flags produced by kinematic movement.
 *
 * @param hitLeft collided while moving left
 * @param hitRight collided while moving right
 * @param hitGround collided while moving down
 * @param hitCeiling collided while moving up
 */
public record MovementResult(boolean hitLeft, boolean hitRight, boolean hitGround, boolean hitCeiling) {
}
