package umbra.physics;

/**
 * Solid-tile collision grid. Version 1 intentionally uses deterministic AABB
 * tile collision before slopes or dynamic rigid bodies.
 */
public final class CollisionGrid {
    private final int widthTiles;
    private final int heightTiles;
    private final int tileSize;
    private final boolean[] solid;

    public CollisionGrid(int widthTiles, int heightTiles, int tileSize) {
        if (widthTiles <= 0 || heightTiles <= 0 || tileSize <= 0) {
            throw new IllegalArgumentException("grid dimensions and tileSize must be positive");
        }
        this.widthTiles = widthTiles;
        this.heightTiles = heightTiles;
        this.tileSize = tileSize;
        this.solid = new boolean[widthTiles * heightTiles];
    }

    public int widthTiles() {
        return widthTiles;
    }

    public int heightTiles() {
        return heightTiles;
    }

    public int tileSize() {
        return tileSize;
    }

    public void setSolid(int tileX, int tileY, boolean value) {
        if (!containsCell(tileX, tileY)) {
            throw new IllegalArgumentException("tile outside grid: " + tileX + "," + tileY);
        }
        solid[index(tileX, tileY)] = value;
    }

    public boolean isSolidCell(int tileX, int tileY) {
        if (!containsCell(tileX, tileY)) {
            return true;
        }
        return solid[index(tileX, tileY)];
    }

    public boolean collides(Aabb bounds) {
        int minX = floorTile(bounds.x());
        int maxX = floorTile(bounds.right() - 0.001f);
        int minY = floorTile(bounds.y());
        int maxY = floorTile(bounds.top() - 0.001f);

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (isSolidCell(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }

    private int floorTile(float worldValue) {
        return (int) Math.floor(worldValue / tileSize);
    }

    private boolean containsCell(int tileX, int tileY) {
        return tileX >= 0 && tileY >= 0 && tileX < widthTiles && tileY < heightTiles;
    }

    private int index(int tileX, int tileY) {
        return tileY * widthTiles + tileX;
    }
}
