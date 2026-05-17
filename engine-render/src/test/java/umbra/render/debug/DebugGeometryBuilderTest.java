package umbra.render.debug;

import org.junit.jupiter.api.Test;
import umbra.physics.Aabb;
import umbra.physics.CollisionGrid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class DebugGeometryBuilderTest {
    @Test
    void solidTilesCreateFilledRects() {
        CollisionGrid grid = new CollisionGrid(3, 2, 32);
        grid.setSolid(1, 0, true);
        grid.setSolid(2, 1, true);
        DebugDrawList drawList = new DebugDrawList();

        new DebugGeometryBuilder().addSolidTiles(drawList, grid, color());

        assertEquals(2, drawList.rects().size());
        assertEquals(new DebugRect(32.0f, 0.0f, 32.0f, 32.0f, color(), DebugShapeStyle.FILLED), drawList.rects().get(0));
        assertEquals(new DebugRect(64.0f, 32.0f, 32.0f, 32.0f, color(), DebugShapeStyle.FILLED), drawList.rects().get(1));
    }

    @Test
    void tileGridCreatesVerticalAndHorizontalLines() {
        CollisionGrid grid = new CollisionGrid(3, 2, 32);
        DebugDrawList drawList = new DebugDrawList();

        new DebugGeometryBuilder().addTileGrid(drawList, grid, color());

        assertEquals(7, drawList.lines().size());
        assertEquals(new DebugLine(0.0f, 0.0f, 0.0f, 64.0f, color()), drawList.lines().get(0));
        assertEquals(new DebugLine(0.0f, 64.0f, 96.0f, 64.0f, color()), drawList.lines().get(6));
    }

    @Test
    void aabbCreatesLineRect() {
        DebugDrawList drawList = new DebugDrawList();

        new DebugGeometryBuilder().addAabb(drawList, new Aabb(4.0f, 8.0f, 12.0f, 16.0f), color());

        assertEquals(new DebugRect(4.0f, 8.0f, 12.0f, 16.0f, color(), DebugShapeStyle.LINE), drawList.rects().get(0));
    }

    @Test
    void validatesDebugPrimitives() {
        assertThrows(IllegalArgumentException.class, () -> new DebugColor(-0.1f, 0.0f, 0.0f, 1.0f));
        assertThrows(IllegalArgumentException.class, () -> new DebugRect(0.0f, 0.0f, 0.0f, 1.0f, color(), DebugShapeStyle.LINE));
    }

    private DebugColor color() {
        return new DebugColor(0.2f, 0.3f, 0.4f, 1.0f);
    }
}
