package umbra.project;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class CreatureDefinitionLoaderTest {
    private final CreatureDefinitionLoader loader = new CreatureDefinitionLoader();

    @Test
    void loadsCreatureDefinition() {
        CreatureDefinition creature = loader.load(new StringReader(validCreatureJson()));

        assertEquals("goblin", creature.id());
        assertEquals("Goblin", creature.displayName());
        assertEquals("monster", creature.category());
        assertEquals(List.of("ground", "weapon"), creature.tags());
        assertEquals("attack", creature.states().animationMapping().get("attack"));
        assertEquals(3, creature.combat().maxHealth());
    }

    @Test
    void rejectsShieldAiWithoutShieldState() {
        assertThrows(CreatureDefinitionValidationException.class, () -> loader.load(new StringReader(
                validCreatureJson().replace("\"uses_shield\": false", "\"uses_shield\": true")
        )));
    }

    @Test
    void rejectsDisabledStateWithAnimationMapping() {
        assertThrows(CreatureDefinitionValidationException.class, () -> loader.load(new StringReader(
                validCreatureJson().replace("\"disabled\": [\"shield\"]", "\"disabled\": [\"attack\"]")
        )));
    }

    private String validCreatureJson() {
        return """
                {
                  "id": "goblin",
                  "display_name": "Goblin",
                  "category": "monster",
                  "asset_root": "monster/Goblin",
                  "animation_metadata": "metadata/goblin.anim.json",
                  "tags": ["ground", "weapon"],
                  "physical": {
                    "body_width": 24,
                    "body_height": 36,
                    "hurtbox_width": 26,
                    "hurtbox_height": 38,
                    "movement_speed": 62,
                    "flying": false
                  },
                  "ai": {
                    "profile": "ground_weapon",
                    "vision_range": 210,
                    "caution_range": 72,
                    "attack_range": 58,
                    "evade_probability": 0.18,
                    "uses_shield": false
                  },
                  "states": {
                    "required": ["idle", "move", "attack", "hurt", "death"],
                    "optional": [],
                    "disabled": ["shield"],
                    "animation_mapping": {
                      "idle": "idle",
                      "move": "move",
                      "attack": "attack",
                      "hurt": "take_hit",
                      "death": "death"
                    }
                  },
                  "combat": {
                    "max_health": 3,
                    "contact_damage": 1,
                    "attack_damage": 1,
                    "attack_active_event": "attack_active"
                  }
                }
                """;
    }
}
