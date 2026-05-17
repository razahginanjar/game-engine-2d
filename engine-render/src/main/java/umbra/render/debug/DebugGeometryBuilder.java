package umbra.render.debug;

import umbra.physics.Aabb;
import umbra.physics.CollisionGrid;

import java.util.Objects;

/**
 * Builds reusable debug geometry from engine data.
 */
public final class DebugGeometryBuilder {
    public void addSolidTiles(DebugDrawList drawList, CollisionGrid grid, DebugColor color) {
        Objects.requireNonNull(drawList, "drawList must not be null");
        Objects.requireNonNull(grid, "grid must not be null");
        Objects.requireNonNull(color, "color must not be null");

        for (int y = 0; y < grid.heightTiles(); y++) {
            for (int x = 0; x < grid.widthTiles(); x++) {
                if (grid.isSolidCell(x, y)) {
                    drawList.addRect(new DebugRect(
                            x * grid.tileSize(),
                            y * grid.tileSize(),
                            grid.tileSize(),
                            grid.tileSize(),
                            color,
                            DebugShapeStyle.FILLED
                    ));
                }
            }
        }
    }

    public void addTileGrid(DebugDrawList drawList, CollisionGrid grid, DebugColor color) {
        Objects.requireNonNull(drawList, "drawList must not be null");
        Objects.requireNonNull(grid, "grid must not be null");
        Objects.requireNonNull(color, "color must not be null");

        float width = grid.widthTiles() * grid.tileSize();
        float height = grid.heightTiles() * grid.tileSize();
        for (int x = 0; x <= grid.widthTiles(); x++) {
            float worldX = x * grid.tileSize();
            drawList.addLine(new DebugLine(worldX, 0.0f, worldX, height, color));
        }
        for (int y = 0; y <= grid.heightTiles(); y++) {
            float worldY = y * grid.tileSize();
            drawList.addLine(new DebugLine(0.0f, worldY, width, worldY, color));
        }
    }

    public void addAabb(DebugDrawList drawList, Aabb bounds, DebugColor color) {
        Objects.requireNonNull(drawList, "drawList must not be null");
        Objects.requireNonNull(bounds, "bounds must not be null");
        Objects.requireNonNull(color, "color must not be null");
        drawList.addRect(new DebugRect(bounds.x(), bounds.y(), bounds.width(), bounds.height(), color, DebugShapeStyle.LINE));
    }
}
