package umbra.project;

import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class RoomVisualDefinitionLoaderTest {
    private final RoomVisualDefinitionLoader loader = new RoomVisualDefinitionLoader();

    @Test
    void loadsRoomVisualDefinition() {
        RoomVisualDefinition visual = loader.load(new StringReader(validRoomVisualJson()));

        assertEquals("forest_test_01", visual.roomId());
        assertEquals("#89A7C6FF", visual.ambientColor());
        assertEquals(2, visual.layers().size());
        assertEquals("sky", visual.layers().get(0).id());
        assertEquals("repeat_x", visual.layers().get(0).repeatMode());
    }

    @Test
    void rejectsDuplicateLayerOrder() {
        assertThrows(RoomVisualDefinitionValidationException.class, () -> loader.load(new StringReader(
                validRoomVisualJson().replace("\"order\": 10", "\"order\": -100")
        )));
    }

    @Test
    void rejectsInvalidOpacity() {
        assertThrows(RoomVisualDefinitionValidationException.class, () -> loader.load(new StringReader(
                validRoomVisualJson().replace("\"opacity\": 0.70", "\"opacity\": 2.0")
        )));
    }

    private String validRoomVisualJson() {
        return """
                {
                  "room_id": "forest_test_01",
                  "ambient_color": "#89A7C6FF",
                  "layers": [
                    {
                      "id": "sky",
                      "type": "background",
                      "asset_path": "background/sky/1.png",
                      "order": -100,
                      "parallax_x": 0.20,
                      "parallax_y": 0.10,
                      "repeat_mode": "repeat_x",
                      "scale_x": 1.0,
                      "scale_y": 1.0,
                      "opacity": 0.70,
                      "tint": "#FFFFFFFF"
                    },
                    {
                      "id": "mist",
                      "type": "foreground",
                      "asset_path": "background/mist/1.png",
                      "order": 10,
                      "parallax_x": 0.80,
                      "parallax_y": 0.60,
                      "repeat_mode": "repeat_x",
                      "opacity": 0.35,
                      "tint": "#BBD6FFFF"
                    }
                  ]
                }
                """;
    }
}
