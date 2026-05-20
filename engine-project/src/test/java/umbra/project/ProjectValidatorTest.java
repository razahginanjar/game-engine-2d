package umbra.project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ProjectValidatorTest {
    private final ProjectValidator validator = new ProjectValidator();

    @TempDir
    private Path projectRoot;

    @Test
    void validatesProjectManifest() throws IOException {
        Files.createDirectories(projectRoot.resolve("assets"));
        writeManifest("""
                {
                  "title": "Umbra2D Sample Metroidvania",
                  "start_room_id": "forest_test_01",
                  "default_spawn_id": "entry_left",
                  "asset_root": "assets",
                  "save": {
                    "enabled": true,
                    "path": ".umbra2d/sample-save.json"
                  },
                  "enabled_modules": ["room", "animation", "ai", "combat", "boss", "progression", "save"]
                }
                """);

        ProjectValidationReport report = validator.validate(projectRoot, "game.manifest.json");

        assertTrue(report.valid());
        assertTrue(report.warnings().isEmpty());
        assertEquals("Umbra2D Sample Metroidvania", report.manifest().orElseThrow().title());
    }

    @Test
    void reportsUnknownModules() throws IOException {
        Files.createDirectories(projectRoot.resolve("assets"));
        writeManifest("""
                {
                  "title": "Umbra2D Sample Metroidvania",
                  "start_room_id": "forest_test_01",
                  "default_spawn_id": "entry_left",
                  "asset_root": "assets",
                  "enabled_modules": ["room", "unknown_runtime"]
                }
                """);

        ProjectValidationReport report = validator.validate(projectRoot, "game.manifest.json");

        assertEquals("module.unknown", report.errors().get(0).code());
    }

    @Test
    void reportsEscapingSavePath() throws IOException {
        Files.createDirectories(projectRoot.resolve("assets"));
        writeManifest("""
                {
                  "title": "Umbra2D Sample Metroidvania",
                  "start_room_id": "forest_test_01",
                  "default_spawn_id": "entry_left",
                  "asset_root": "assets",
                  "save": {
                    "enabled": true,
                    "path": "../sample-save.json"
                  },
                  "enabled_modules": ["save"]
                }
                """);

        ProjectValidationReport report = validator.validate(projectRoot, "game.manifest.json");

        assertEquals("save.path.escapes_project", report.errors().get(0).code());
    }

    @Test
    void warnsWhenAssetRootIsMissing() throws IOException {
        writeManifest("""
                {
                  "title": "Umbra2D Sample Metroidvania",
                  "start_room_id": "forest_test_01",
                  "default_spawn_id": "entry_left",
                  "asset_root": "assets",
                  "enabled_modules": ["room"]
                }
                """);

        ProjectValidationReport report = validator.validate(projectRoot, "game.manifest.json");

        assertTrue(report.valid());
        assertEquals("asset_root.missing", report.warnings().get(0).code());
    }

    private void writeManifest(String json) throws IOException {
        Files.writeString(projectRoot.resolve("game.manifest.json"), json);
    }
}
