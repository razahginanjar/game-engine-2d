package umbra.render.sprite;

import org.junit.jupiter.api.Test;
import umbra.render.debug.DebugColor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class SpriteDrawCommandTest {
    @Test
    void storesSpriteCommandData() {
        SpriteDrawCommand command = new SpriteDrawCommand(
                "player_idle",
                0,
                0,
                120,
                80,
                10.0f,
                20.0f,
                120.0f,
                80.0f,
                true,
                false,
                white()
        );

        assertEquals("player_idle", command.textureId());
        assertEquals(120, command.sourceWidth());
        assertEquals(80.0f, command.height());
        assertEquals(true, command.flipX());
    }

    @Test
    void drawListPreservesCommandOrder() {
        SpriteDrawList list = new SpriteDrawList();
        SpriteDrawCommand first = command("first");
        SpriteDrawCommand second = command("second");

        list.add(first);
        list.add(second);

        assertEquals(first, list.commands().get(0));
        assertEquals(second, list.commands().get(1));
    }

    @Test
    void rejectsInvalidCommandData() {
        assertThrows(IllegalArgumentException.class, () -> new SpriteDrawCommand("", 0, 0, 1, 1, 0, 0, 1, 1, false, false, white()));
        assertThrows(IllegalArgumentException.class, () -> new SpriteDrawCommand("id", 0, 0, 0, 1, 0, 0, 1, 1, false, false, white()));
        assertThrows(IllegalArgumentException.class, () -> new SpriteDrawCommand("id", 0, 0, 1, 1, 0, 0, 0, 1, false, false, white()));
    }

    private SpriteDrawCommand command(String textureId) {
        return new SpriteDrawCommand(textureId, 0, 0, 1, 1, 0, 0, 1, 1, false, false, white());
    }

    private DebugColor white() {
        return new DebugColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
