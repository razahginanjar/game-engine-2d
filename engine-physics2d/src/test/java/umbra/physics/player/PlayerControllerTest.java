package umbra.physics.player;

import org.junit.jupiter.api.Test;
import umbra.physics.CollisionGrid;
import umbra.physics.KinematicBody;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlayerControllerTest {
    @Test
    void playerFallsOntoSolidFloor() {
        CollisionGrid grid = floorGrid();
        KinematicBody body = new KinematicBody(48, 96, 18, 38);
        PlayerController controller = new PlayerController(PlayerControllerConfig.metroidvaniaDefaults());

        for (int i = 0; i < 120; i++) {
            controller.update(PlayerInput.none(), body, grid, 1.0f / 60.0f);
        }

        assertTrue(controller.grounded());
        assertTrue(body.y() >= 32.0f);
    }

    @Test
    void jumpBufferTriggersWhenLanding() {
        CollisionGrid grid = floorGrid();
        KinematicBody body = new KinematicBody(48, 44, 18, 38);
        PlayerController controller = new PlayerController(PlayerControllerConfig.metroidvaniaDefaults());

        controller.update(new PlayerInput(false, false, true, true), body, grid, 1.0f / 60.0f);
        for (int i = 0; i < 8; i++) {
            controller.update(new PlayerInput(false, false, false, true), body, grid, 1.0f / 60.0f);
        }

        assertTrue(body.velocityY() > 0.0f);
    }

    private CollisionGrid floorGrid() {
        CollisionGrid grid = new CollisionGrid(10, 8, 32);
        for (int x = 0; x < grid.widthTiles(); x++) {
            grid.setSolid(x, 0, true);
        }
        return grid;
    }
}
