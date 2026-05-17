package umbra.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class FixedStepEngineTest {
    @Test
    void fixedStepAccumulatesFrameDelta() {
        SceneManager sceneManager = new SceneManager();
        CountingScene scene = new CountingScene();
        sceneManager.setScene(scene);

        FixedStepEngine engine = new FixedStepEngine(
                new EngineConfig("test", 320, 180, 16, 60),
                sceneManager
        );

        engine.update(1.0f / 30.0f);

        assertEquals(2, scene.updates);
    }

    private static final class CountingScene implements Scene {
        int updates;

        @Override
        public void update(float deltaSeconds) {
            updates++;
        }

        @Override
        public void render() {
        }
    }
}
