package umbra.render.debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mutable collection of debug draw commands for one frame.
 */
public final class DebugDrawList {
    private final List<DebugRect> rects = new ArrayList<>();
    private final List<DebugLine> lines = new ArrayList<>();

    public void addRect(DebugRect rect) {
        rects.add(rect);
    }

    public void addLine(DebugLine line) {
        lines.add(line);
    }

    public void clear() {
        rects.clear();
        lines.clear();
    }

    public List<DebugRect> rects() {
        return Collections.unmodifiableList(rects);
    }

    public List<DebugLine> lines() {
        return Collections.unmodifiableList(lines);
    }
}
