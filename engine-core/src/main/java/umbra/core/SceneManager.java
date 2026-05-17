package umbra.core;

import java.util.Objects;

/**
 * Owns the active scene and guarantees onExit/onEnter ordering during scene
 * switches.
 */
public final class SceneManager {
    private Scene activeScene;

    public void setScene(Scene nextScene) {
        Objects.requireNonNull(nextScene, "nextScene");
        if (activeScene != null) {
            activeScene.onExit();
        }
        activeScene = nextScene;
        activeScene.onEnter();
    }

    public void update(float deltaSeconds) {
        if (activeScene != null) {
            activeScene.update(deltaSeconds);
        }
    }

    public void render() {
        if (activeScene != null) {
            activeScene.render();
        }
    }

    public boolean hasScene() {
        return activeScene != null;
    }
}
