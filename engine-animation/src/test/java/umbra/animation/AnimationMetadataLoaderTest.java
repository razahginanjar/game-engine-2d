package umbra.animation;

import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnimationMetadataLoaderTest {
    @Test
    void loadsSheetAndTextureSequenceClips() {
        String json = """
                {
                  "id": "sample",
                  "clips": [
                    {
                      "id": "run",
                      "texture_id": "player_run",
                      "frame_width": 120,
                      "frame_height": 80,
                      "frame_count": 10,
                      "fps": 12,
                      "loop": true,
                      "events": [
                        { "frame": 3, "id": "footstep" }
                      ]
                    },
                    {
                      "id": "slime_move",
                      "texture_ids": ["slime_00000", "slime_00001"],
                      "frame_width": 376,
                      "frame_height": 256,
                      "frame_count": 2,
                      "fps": 10,
                      "loop": true
                    }
                  ]
                }
                """;

        AnimationSetDefinition set = new AnimationMetadataLoader().load(new StringReader(json));

        AnimationClipDefinition run = set.clip("run");
        assertEquals("player_run", run.textureIdForFrame(3));
        assertEquals(360, run.sourceXForFrame(3));
        assertTrue(run.hasEvent(3, "footstep"));

        AnimationClipDefinition slime = set.clip("slime_move");
        assertEquals("slime_00001", slime.textureIdForFrame(1));
        assertEquals(0, slime.sourceXForFrame(1));
    }

    @Test
    void rejectsSequenceFrameCountMismatch() {
        String json = """
                {
                  "id": "sample",
                  "clips": [
                    {
                      "id": "bad",
                      "texture_ids": ["a"],
                      "frame_width": 32,
                      "frame_height": 32,
                      "frame_count": 2,
                      "fps": 8,
                      "loop": true
                    }
                  ]
                }
                """;

        assertThrows(IllegalArgumentException.class, () -> new AnimationMetadataLoader().load(new StringReader(json)));
    }

    @Test
    void rejectsEventsOutsideClipFrameRange() {
        String json = """
                {
                  "id": "sample",
                  "clips": [
                    {
                      "id": "bad",
                      "texture_id": "bad",
                      "frame_width": 32,
                      "frame_height": 32,
                      "frame_count": 2,
                      "fps": 8,
                      "loop": true,
                      "events": [
                        { "frame": 2, "id": "invalid" }
                      ]
                    }
                  ]
                }
                """;

        assertThrows(IllegalArgumentException.class, () -> new AnimationMetadataLoader().load(new StringReader(json)));
    }
}
