package umbra.core;

/**
 * Public scene lifecycle. Game projects use scenes for boot, menus, levels,
 * loading screens, and test rooms.
 */
public interface Scene {
    default void onEnter() {
    }

    void update(float deltaSeconds);

    void render();

    default void onExit() {
    }
}
