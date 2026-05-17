package umbra.sample;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import umbra.core.EngineConfig;
import umbra.core.FixedStepEngine;
import umbra.core.SceneManager;

public final class Umbra2DGame extends ApplicationAdapter {
    private EngineConfig config;
    private FixedStepEngine engine;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private FitViewport viewport;

    @Override
    public void create() {
        config = EngineConfig.metroidvaniaDefaults();
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        viewport = new FitViewport(config.viewportWidth(), config.viewportHeight(), camera);
        viewport.apply();
        camera.position.set(config.viewportWidth() / 2.0f, config.viewportHeight() / 2.0f, 0.0f);
        camera.update();

        SceneManager scenes = new SceneManager();
        scenes.setScene(new TestRoomScene(shapeRenderer, camera, config));
        engine = new FixedStepEngine(config, scenes);
    }

    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        engine.update(Gdx.graphics.getDeltaTime());

        Gdx.gl.glClearColor(0.035f, 0.045f, 0.060f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);
        engine.render();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
