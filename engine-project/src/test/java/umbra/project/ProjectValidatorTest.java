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
        Files.createDirectories(projectRoot.resolve("metadata/creatures"));
        Files.createDirectories(projectRoot.resolve("metadata/animations"));
        Files.createDirectories(projectRoot.resolve("metadata/room_visuals"));
        Files.createDirectories(projectRoot.resolve("assets/background/sky"));
        Files.writeString(projectRoot.resolve("assets/background/sky/1.png"), "fake-png");
        writeAnimation("metadata/animations/goblin.anim.json", true);
        writeCreature("metadata/creatures/goblin.creature.json", "metadata/animations/goblin.anim.json");
        writeRoomVisual("metadata/room_visuals/forest_test_01.visual.json", "background/sky/1.png");
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
                  "enabled_modules": ["room", "animation", "ai", "combat", "boss", "progression", "save"],
                  "creature_definitions": ["metadata/creatures/goblin.creature.json"],
                  "room_visual_definitions": ["metadata/room_visuals/forest_test_01.visual.json"]
                }
                """);

        ProjectValidationReport report = validator.validate(projectRoot, "game.manifest.json");

        assertTrue(report.valid());
        assertTrue(report.warnings().isEmpty());
        assertEquals("Umbra2D Sample Metroidvania", report.manifest().orElseThrow().title());
    }

    @Test
    void reportsCreatureMissingRequiredAnimationMapping() throws IOException {
        Files.createDirectories(projectRoot.resolve("assets"));
        Files.createDirectories(projectRoot.resolve("metadata/creatures"));
        Files.createDirectories(projectRoot.resolve("metadata/animations"));
        writeAnimation("metadata/animations/goblin.anim.json", true);
        writeCreature("metadata/creatures/goblin.creature.json", "metadata/animations/goblin.anim.json",
                """
                    "idle": "idle",
                    "move": "move",
                    "attack": "attack",
                    "death": "death"
                """);
        writeManifestWithCreature("metadata/creatures/goblin.creature.json");

        ProjectValidationReport report = validator.validate(projectRoot, "game.manifest.json");

        assertEquals("creature.state.missing_mapping", report.errors().get(0).code());
    }

    @Test
    void reportsCreatureAttackClipWithoutActiveEvent() throws IOException {
        Files.createDirectories(projectRoot.resolve("assets"));
        Files.createDirectories(projectRoot.resolve("metadata/creatures"));
        Files.createDirectories(projectRoot.resolve("metadata/animations"));
        writeAnimation("metadata/animations/goblin.anim.json", false);
        writeCreature("metadata/creatures/goblin.creature.json", "metadata/animations/goblin.anim.json");
        writeManifestWithCreature("metadata/creatures/goblin.creature.json");

        ProjectValidationReport report = validator.validate(projectRoot, "game.manifest.json");

        assertEquals("creature.attack.missing_active_event", report.errors().get(0).code());
    }

    @Test
    void warnsWhenRoomVisualAssetIsMissing() throws IOException {
        Files.createDirectories(projectRoot.resolve("assets"));
        Files.createDirectories(projectRoot.resolve("metadata/room_visuals"));
        writeRoomVisual("metadata/room_visuals/forest_test_01.visual.json", "background/missing/1.png");
        writeManifestWithRoomVisual("metadata/room_visuals/forest_test_01.visual.json");

        ProjectValidationReport report = validator.validate(projectRoot, "game.manifest.json");

        assertTrue(report.valid());
        assertEquals("room_visual.asset.missing", report.warnings().get(0).code());
    }

    @Test
    void reportsRoomVisualAssetEscapingAssetRoot() throws IOException {
        Files.createDirectories(projectRoot.resolve("assets"));
        Files.createDirectories(projectRoot.resolve("metadata/room_visuals"));
        writeRoomVisual("metadata/room_visuals/forest_test_01.visual.json", "../outside.png");
        writeManifestWithRoomVisual("metadata/room_visuals/forest_test_01.visual.json");

        ProjectValidationReport report = validator.validate(projectRoot, "game.manifest.json");

        assertEquals("room_visual.asset.escapes_assets", report.errors().get(0).code());
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

    private void writeManifestWithCreature(String creatureDefinitionPath) throws IOException {
        writeManifest("""
                {
                  "title": "Umbra2D Sample Metroidvania",
                  "start_room_id": "forest_test_01",
                  "default_spawn_id": "entry_left",
                  "asset_root": "assets",
                  "enabled_modules": ["room", "animation", "ai", "combat"],
                  "creature_definitions": ["%s"]
                }
                """.formatted(creatureDefinitionPath));
    }

    private void writeManifestWithRoomVisual(String roomVisualDefinitionPath) throws IOException {
        writeManifest("""
                {
                  "title": "Umbra2D Sample Metroidvania",
                  "start_room_id": "forest_test_01",
                  "default_spawn_id": "entry_left",
                  "asset_root": "assets",
                  "enabled_modules": ["room", "render"],
                  "room_visual_definitions": ["%s"]
                }
                """.formatted(roomVisualDefinitionPath));
    }

    private void writeCreature(String path, String animationPath) throws IOException {
        writeCreature(path, animationPath, """
                    "idle": "idle",
                    "move": "move",
                    "attack": "attack",
                    "hurt": "take_hit",
                    "death": "death"
                """);
    }

    private void writeCreature(String path, String animationPath, String animationMapping) throws IOException {
        Files.writeString(projectRoot.resolve(path), """
                {
                  "id": "goblin",
                  "display_name": "Goblin",
                  "category": "monster",
                  "asset_root": "monster/Goblin",
                  "animation_metadata": "%s",
                  "tags": ["ground", "weapon"],
                  "physical": {
                    "body_width": 24,
                    "body_height": 36,
                    "hurtbox_width": 26,
                    "hurtbox_height": 38,
                    "movement_speed": 62
                  },
                  "ai": {
                    "profile": "ground_weapon",
                    "vision_range": 210,
                    "caution_range": 72,
                    "attack_range": 58,
                    "evade_probability": 0.18
                  },
                  "states": {
                    "required": ["idle", "move", "attack", "hurt", "death"],
                    "disabled": ["shield"],
                    "animation_mapping": {
                %s
                    }
                  },
                  "combat": {
                    "max_health": 3,
                    "contact_damage": 1,
                    "attack_damage": 1,
                    "attack_active_event": "attack_active"
                  }
                }
                """.formatted(animationPath, animationMapping));
    }

    private void writeAnimation(String path, boolean includeAttackEvent) throws IOException {
        String attackEvents = includeAttackEvent
                ? """
                      ,
                      "events": [
                        { "frame": 1, "id": "attack_active" }
                      ]
                  """
                : "";
        Files.writeString(projectRoot.resolve(path), """
                {
                  "id": "goblin",
                  "clips": [
                    {
                      "id": "idle",
                      "texture_id": "goblin_idle",
                      "frame_width": 150,
                      "frame_height": 150,
                      "frame_count": 4,
                      "fps": 8,
                      "loop": true
                    },
                    {
                      "id": "move",
                      "texture_id": "goblin_move",
                      "frame_width": 150,
                      "frame_height": 150,
                      "frame_count": 8,
                      "fps": 10,
                      "loop": true
                    },
                    {
                      "id": "attack",
                      "texture_id": "goblin_attack",
                      "frame_width": 150,
                      "frame_height": 150,
                      "frame_count": 8,
                      "fps": 14,
                      "loop": false
                %s
                    },
                    {
                      "id": "take_hit",
                      "texture_id": "goblin_take_hit",
                      "frame_width": 150,
                      "frame_height": 150,
                      "frame_count": 4,
                      "fps": 12,
                      "loop": false
                    },
                    {
                      "id": "death",
                      "texture_id": "goblin_death",
                      "frame_width": 150,
                      "frame_height": 150,
                      "frame_count": 4,
                      "fps": 10,
                      "loop": false
                    }
                  ]
                }
                """.formatted(attackEvents));
    }

    private void writeRoomVisual(String path, String assetPath) throws IOException {
        Files.writeString(projectRoot.resolve(path), """
                {
                  "room_id": "forest_test_01",
                  "ambient_color": "#89A7C6FF",
                  "layers": [
                    {
                      "id": "sky",
                      "type": "background",
                      "asset_path": "%s",
                      "order": -100,
                      "parallax_x": 0.20,
                      "parallax_y": 0.10,
                      "repeat_mode": "repeat_x",
                      "opacity": 0.70,
                      "tint": "#FFFFFFFF"
                    }
                  ]
                }
                """.formatted(assetPath));
    }
}
