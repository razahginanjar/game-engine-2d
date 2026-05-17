package umbra.physics.player;

/**
 * Frame input snapshot consumed by the controller. Gameplay code should map
 * real devices into actions before creating this value.
 */
public record PlayerInput(boolean left, boolean right, boolean jumpPressed, boolean jumpHeld) {
    public static PlayerInput none() {
        return new PlayerInput(false, false, false, false);
    }
}
