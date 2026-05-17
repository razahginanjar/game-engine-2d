package umbra.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import umbra.core.EngineConfig;
import umbra.sample.Umbra2DGame;

public final class DesktopLauncher {
    private DesktopLauncher() {
    }

    public static void main(String[] args) {
        EngineConfig config = EngineConfig.metroidvaniaDefaults();
        Lwjgl3ApplicationConfiguration appConfig = new Lwjgl3ApplicationConfiguration();
        appConfig.setTitle(config.gameTitle());
        appConfig.setWindowedMode(config.viewportWidth(), config.viewportHeight());
        appConfig.useVsync(true);
        appConfig.setForegroundFPS(60);
        new Lwjgl3Application(new Umbra2DGame(), appConfig);
    }
}
