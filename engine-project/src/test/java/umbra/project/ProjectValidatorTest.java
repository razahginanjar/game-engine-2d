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
        writeAnimation("metadata/animations/goblin.anim.json", true);
        writeCreature("metadata/creatures/goblin.creature.json", "metadata/animations/goblin.anim.json");
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
                  "creature_definitions": ["metadata/creatures/goblin.creature.json"]
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
}
