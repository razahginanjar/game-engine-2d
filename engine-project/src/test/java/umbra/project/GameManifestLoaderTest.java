package umbra.project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class GameManifestLoaderTest {
    private final GameManifestLoader loader = new GameManifestLoader();

    @TempDir
    private Path tempDir;

    @Test
    void loadsProjectManifest() {
        GameManifest manifest = loader.load(new StringReader("""
                {
                  "title": "Umbra2D Sample Metroidvania",
                  "start_room_id": "forest_test_01",
                  "default_spawn_id": "entry_left",
                  "asset_root": "../assets",
                  "save": {
                    "enabled": true,
                    "path": ".umbra2d/sample-save.json"
                  },
                  "enabled_modules": [
                    "room",
                    "animation",
                    "ai",
                    "combat",
                    "boss",
                    "progression",
                    "save"
                  ],
                  "creature_definitions": [
                    "metadata/creatures/goblin.creature.json"
                  ]
                }
                """));

        assertEquals("Umbra2D Sample Metroidvania", manifest.title());
        assertEquals("forest_test_01", manifest.startRoomId());
        assertEquals("entry_left", manifest.defaultSpawnId());
        assertEquals("../assets", manifest.assetRoot());
        assertEquals(new SavePolicy(true, ".umbra2d/sample-save.json"), manifest.savePolicy());
        assertEquals(List.of("room", "animation", "ai", "combat", "boss", "progression", "save"),
                manifest.enabledModules());
        assertEquals(List.of("metadata/creatures/goblin.creature.json"), manifest.creatureDefinitions());
    }

    @Test
    void loadsProjectManifestFromProjectRelativePath() throws IOException {
        Files.createDirectories(tempDir.resolve("config"));
        Files.writeString(tempDir.resolve("config/game.manifest.json"), """
                {
                  "title": "Umbra2D Sample Metroidvania",
                  "start_room_id": "forest_test_01",
                  "default_spawn_id": "entry_left",
                  "asset_root": "../assets",
                  "enabled_modules": ["room"]
                }
                """);

        GameManifest manifest = loader.loadProjectManifest(tempDir, "config/game.manifest.json");

        assertEquals("Umbra2D Sample Metroidvania", manifest.title());
        assertEquals(List.of("room"), manifest.enabledModules());
    }

    @Test
    void rejectsManifestPathEscapingProjectRoot() {
        assertThrows(GameManifestValidationException.class,
                () -> loader.loadProjectManifest(tempDir, "../game.manifest.json"));
    }

    @Test
    void rejectsAbsoluteAssetRoot() {
        assertThrows(GameManifestValidationException.class, () -> loader.load(new StringReader("""
                {
                  "title": "Umbra2D Sample Metroidvania",
                  "start_room_id": "forest_test_01",
                  "default_spawn_id": "entry_left",
                  "asset_root": "C:/assets",
                  "enabled_modules": ["room"]
                }
                """)));
    }

    @Test
    void rejectsEmptyEnabledModules() {
        assertThrows(GameManifestValidationException.class, () -> loader.load(new StringReader("""
                {
                  "title": "Umbra2D Sample Metroidvania",
                  "start_room_id": "forest_test_01",
                  "default_spawn_id": "entry_left",
                  "asset_root": "../assets",
                  "enabled_modules": []
                }
                """)));
    }

    @Test
    void rejectsEnabledSaveWithoutPath() {
        assertThrows(GameManifestValidationException.class, () -> loader.load(new StringReader("""
                {
                  "title": "Umbra2D Sample Metroidvania",
                  "start_room_id": "forest_test_01",
                  "default_spawn_id": "entry_left",
                  "asset_root": "../assets",
                  "save": {
                    "enabled": true
                  },
                  "enabled_modules": ["save"]
                }
                """)));
    }
}
