package umbra.render.sprite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mutable collection of sprite draw commands for one frame.
 */
public final class SpriteDrawList {
    private final List<SpriteDrawCommand> commands = new ArrayList<>();

    public void add(SpriteDrawCommand command) {
        commands.add(command);
    }

    public void clear() {
        commands.clear();
    }

    public List<SpriteDrawCommand> commands() {
        return Collections.unmodifiableList(commands);
    }
}
