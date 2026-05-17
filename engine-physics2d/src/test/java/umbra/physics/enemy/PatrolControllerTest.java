package umbra.physics.enemy;

import org.junit.jupiter.api.Test;
import umbra.physics.CollisionGrid;
import umbra.physics.KinematicBody;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PatrolControllerTest {
    @Test
    void patrolMovesInInitialDirection() {
        CollisionGrid grid = floorGrid(8);
        KinematicBody body = new KinematicBody(64.0f, 32.0f, 20.0f, 16.0f);
        PatrolController controller = new PatrolController(PatrolControllerConfig.slimeDefaults(), 1);

        controller.update(body, grid, 1.0f);

        assertTrue(body.x() > 64.0f);
        assertEquals(1, controller.direction());
    }

    @Test
    void patrolReversesWhenHittingWall() {
        CollisionGrid grid = floorGrid(8);
        grid.setSolid(4, 1, true);
        KinematicBody body = new KinematicBody(94.0f, 32.0f, 20.0f, 16.0f);
        PatrolController controller = new PatrolController(PatrolControllerConfig.slimeDefaults(), 1);

        controller.update(body, grid, 1.0f);

        assertEquals(-1, controller.direction());
    }

    @Test
    void patrolReversesBeforeLedge() {
        CollisionGrid grid = partialFloorGrid(8, 4);
        KinematicBody body = new KinematicBody(108.0f, 32.0f, 20.0f, 16.0f);
        PatrolController controller = new PatrolController(PatrolControllerConfig.slimeDefaults(), 1);

        controller.update(body, grid, 1.0f / 60.0f);

        assertEquals(-1, controller.direction());
    }

    @Test
    void resetChangesDirection() {
        PatrolController controller = new PatrolController(PatrolControllerConfig.slimeDefaults(), -1);

        controller.reset(1);

        assertEquals(1, controller.direction());
    }

    @Test
    void rejectsInvalidInputs() {
        assertThrows(IllegalArgumentException.class, () -> new PatrolControllerConfig(0.0f, 1.0f, 1.0f));
        assertThrows(IllegalArgumentException.class, () -> new PatrolControllerConfig(1.0f, 0.0f, 1.0f));
        assertThrows(IllegalArgumentException.class, () -> new PatrolControllerConfig(1.0f, 1.0f, 0.0f));

        PatrolController controller = new PatrolController(PatrolControllerConfig.slimeDefaults(), 1);
        assertThrows(IllegalArgumentException.class, () -> controller.update(
                new KinematicBody(32.0f, 32.0f, 20.0f, 16.0f),
                floorGrid(4),
                -0.1f
        ));
    }

    private CollisionGrid floorGrid(int widthTiles) {
        return partialFloorGrid(widthTiles, widthTiles);
    }

    private CollisionGrid partialFloorGrid(int widthTiles, int solidFloorTiles) {
        CollisionGrid grid = new CollisionGrid(widthTiles, 4, 32);
        for (int x = 0; x < solidFloorTiles; x++) {
            grid.setSolid(x, 0, true);
        }
        return grid;
    }
}
