package umbra.physics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CollisionGridTest {
    @Test
    void outsideGridIsSolid() {
        CollisionGrid grid = new CollisionGrid(4, 4, 16);

        assertTrue(grid.collides(new Aabb(-1, 16, 8, 8)));
        assertTrue(grid.collides(new Aabb(16, -1, 8, 8)));
    }

    @Test
    void detectsSolidTileOverlap() {
        CollisionGrid grid = new CollisionGrid(4, 4, 16);
        grid.setSolid(1, 1, true);

        assertTrue(grid.collides(new Aabb(17, 17, 8, 8)));
        assertFalse(grid.collides(new Aabb(33, 33, 8, 8)));
    }
}
