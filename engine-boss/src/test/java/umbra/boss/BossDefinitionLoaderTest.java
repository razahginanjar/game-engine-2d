package umbra.boss;

import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class BossDefinitionLoaderTest {
    @Test
    void loadsBossDefinitionWithPhasesAndAttacks() {
        BossDefinition definition = new BossDefinitionLoader().load(new StringReader("""
                {
                  "id": "impaler",
                  "display_name": "Impaler",
                  "max_health": 18,
                  "phases": [
                    { "id": "phase_1", "starts_at_health_ratio": 1.0 },
                    { "id": "phase_2", "starts_at_health_ratio": 0.5 }
                  ],
                  "attacks": [{
                    "id": "impaler_stab",
                    "phase_id": "phase_1",
                    "clip_id": "attack2",
                    "hitbox_profile": "impaler_attack2",
                    "min_range": 0,
                    "max_range": 72,
                    "cooldown_seconds": 0.82,
                    "frame_count": 8,
                    "fps": 10,
                    "damage": 1,
                    "knockback_x": 220,
                    "knockback_y": 70,
                    "hit_pause_seconds": 0.065,
                    "hit_stun_seconds": 0.25,
                    "damage_type": "boss_spear",
                    "hitbox_windows": [{
                      "start_frame": 2,
                      "end_frame": 4,
                      "shapes": [{ "type": "facing_body" }]
                    }]
                  }]
                }
                """));

        assertEquals("impaler", definition.id());
        assertEquals(18, definition.maxHealth());
        assertEquals(2, definition.phases().size());
        assertEquals(1, definition.attacks().size());
        assertEquals("impaler_stab", definition.attackPatterns().get(0).id());
    }

    @Test
    void rejectsAttackReferencingMissingPhase() {
        BossDefinitionLoader loader = new BossDefinitionLoader();

        assertThrows(BossDefinitionValidationException.class, () -> loader.load(new StringReader("""
                {
                  "id": "impaler",
                  "display_name": "Impaler",
                  "max_health": 18,
                  "phases": [{ "id": "phase_1", "starts_at_health_ratio": 1.0 }],
                  "attacks": [{
                    "id": "impaler_stab",
                    "phase_id": "phase_2",
                    "clip_id": "attack2",
                    "hitbox_profile": "impaler_attack2",
                    "min_range": 0,
                    "max_range": 72,
                    "cooldown_seconds": 0.82,
                    "frame_count": 8,
                    "fps": 10,
                    "damage": 1,
                    "knockback_x": 220,
                    "knockback_y": 70,
                    "hit_pause_seconds": 0.065,
                    "hit_stun_seconds": 0.25,
                    "hitbox_windows": [{
                      "start_frame": 2,
                      "end_frame": 4,
                      "shapes": [{ "type": "facing_body" }]
                    }]
                  }]
                }
                """)));
    }

    @Test
    void rejectsDuplicateAttackIds() {
        BossDefinitionLoader loader = new BossDefinitionLoader();

        assertThrows(BossDefinitionValidationException.class, () -> loader.load(new StringReader("""
                {
                  "id": "impaler",
                  "display_name": "Impaler",
                  "max_health": 18,
                  "phases": [{ "id": "phase_1", "starts_at_health_ratio": 1.0 }],
                  "attacks": [
                    {
                      "id": "impaler_stab",
                      "phase_id": "phase_1",
                      "clip_id": "attack2",
                      "hitbox_profile": "impaler_attack2",
                      "min_range": 0,
                      "max_range": 72,
                      "cooldown_seconds": 0.82,
                      "frame_count": 8,
                      "fps": 10,
                      "damage": 1,
                      "knockback_x": 220,
                      "knockback_y": 70,
                      "hit_pause_seconds": 0.065,
                      "hit_stun_seconds": 0.25,
                      "hitbox_windows": [{
                        "start_frame": 2,
                        "end_frame": 4,
                        "shapes": [{ "type": "facing_body" }]
                      }]
                    },
                    {
                      "id": "impaler_stab",
                      "phase_id": "phase_1",
                      "clip_id": "attack1",
                      "hitbox_profile": "impaler_attack1",
                      "min_range": 0,
                      "max_range": 104,
                      "cooldown_seconds": 1.18,
                      "frame_count": 25,
                      "fps": 12,
                      "damage": 1,
                      "knockback_x": 220,
                      "knockback_y": 70,
                      "hit_pause_seconds": 0.065,
                      "hit_stun_seconds": 0.25,
                      "hitbox_windows": [{
                        "start_frame": 3,
                        "end_frame": 4,
                        "shapes": [{ "type": "facing_body" }]
                      }]
                    }
                  ]
                }
                """)));
    }

    @Test
    void rejectsHitboxWindowOutsideAttackFrames() {
        BossDefinitionLoader loader = new BossDefinitionLoader();

        assertThrows(BossDefinitionValidationException.class, () -> loader.load(new StringReader("""
                {
                  "id": "impaler",
                  "display_name": "Impaler",
                  "max_health": 18,
                  "phases": [{ "id": "phase_1", "starts_at_health_ratio": 1.0 }],
                  "attacks": [{
                    "id": "impaler_stab",
                    "phase_id": "phase_1",
                    "clip_id": "attack2",
                    "hitbox_profile": "impaler_attack2",
                    "min_range": 0,
                    "max_range": 72,
                    "cooldown_seconds": 0.82,
                    "frame_count": 8,
                    "fps": 10,
                    "damage": 1,
                    "knockback_x": 220,
                    "knockback_y": 70,
                    "hit_pause_seconds": 0.065,
                    "hit_stun_seconds": 0.25,
                    "hitbox_windows": [{
                      "start_frame": 9,
                      "end_frame": 10,
                      "shapes": [{ "type": "facing_body" }]
                    }]
                  }]
                }
                """)));
    }
}
