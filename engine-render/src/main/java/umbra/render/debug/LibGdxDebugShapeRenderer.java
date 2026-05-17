package umbra.render.debug;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.Objects;

/**
 * LibGDX adapter for engine debug draw commands.
 */
public final class LibGdxDebugShapeRenderer {
    private final ShapeRenderer shapes;

    public LibGdxDebugShapeRenderer(ShapeRenderer shapes) {
        this.shapes = Objects.requireNonNull(shapes, "shapes must not be null");
    }

    public void render(DebugDrawList drawList) {
        Objects.requireNonNull(drawList, "drawList must not be null");
        renderRects(drawList, DebugShapeStyle.FILLED, ShapeRenderer.ShapeType.Filled);
        renderRects(drawList, DebugShapeStyle.LINE, ShapeRenderer.ShapeType.Line);
        renderLines(drawList);
    }

    private void renderRects(DebugDrawList drawList, DebugShapeStyle style, ShapeRenderer.ShapeType shapeType) {
        boolean began = false;
        for (DebugRect rect : drawList.rects()) {
            if (rect.style() != style) {
                continue;
            }
            if (!began) {
                shapes.begin(shapeType);
                began = true;
            }
            setColor(rect.color());
            shapes.rect(rect.x(), rect.y(), rect.width(), rect.height());
        }
        if (began) {
            shapes.end();
        }
    }

    private void renderLines(DebugDrawList drawList) {
        if (drawList.lines().isEmpty()) {
            return;
        }
        shapes.begin(ShapeRenderer.ShapeType.Line);
        for (DebugLine line : drawList.lines()) {
            setColor(line.color());
            shapes.line(line.x1(), line.y1(), line.x2(), line.y2());
        }
        shapes.end();
    }

    private void setColor(DebugColor color) {
        shapes.setColor(color.r(), color.g(), color.b(), color.a());
    }
}
