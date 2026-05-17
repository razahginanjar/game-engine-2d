package umbra.physics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class KinematicImpulseMoverTest {
    @Test
    void appliesVelocityAndGravity() {
        CollisionGrid grid = floorGrid(10);
        KinematicBody body = new KinematicBody(64.0f, 96.0f, 16.0f, 16.0f);
        body.setVelocityX(60.0f);
        body.setVelocityY(120.0f);

        new KinematicImpulseMover().update(body, grid, new KinematicImpulseConfig(100.0f, 300.0f), 0.5f);

        assertTrue(body.x() > 64.0f);
        assertTrue(body.y() > 96.0f);
        assertEquals(70.0f, body.velocityY(), 0.0001f);
    }

    @Test
    void clearsHorizontalVelocityWhenHittingWall() {
        CollisionGrid grid = floorGrid(6);
        grid.setSolid(3, 1, true);
        KinematicBody body = new KinematicBody(80.0f, 32.0f, 16.0f, 16.0f);
        body.setVelocityX(120.0f);

        MovementResult result = new KinematicImpulseMover().update(body, grid, config(), 0.5f);

        assertTrue(result.hitRight());
        assertEquals(0.0f, body.velocityX(), 0.0001f);
    }

    @Test
    void clearsVerticalVelocityWhenLanding() {
        CollisionGrid grid = floorGrid(6);
        KinematicBody body = new KinematicBody(64.0f, 36.0f, 16.0f, 16.0f);
        body.setVelocityY(-120.0f);

        MovementResult result = new KinematicImpulseMover().update(body, grid, config(), 0.5f);

        assertTrue(result.hitGround());
        assertEquals(0.0f, body.velocityY(), 0.0001f);
    }

    @Test
    void rejectsInvalidInputs() {
        assertThrows(IllegalArgumentException.class, () -> new KinematicImpulseConfig(0.0f, 1.0f));
        assertThrows(IllegalArgumentException.class, () -> new KinematicImpulseConfig(1.0f, 0.0f));
        assertThrows(IllegalArgumentException.class, () -> new KinematicImpulseMover().update(
                new KinematicBody(32.0f, 32.0f, 16.0f, 16.0f),
                floorGrid(4),
                config(),
                -0.1f
        ));
    }

    private KinematicImpulseConfig config() {
        return new KinematicImpulseConfig(250.0f, 300.0f);
    }

    private CollisionGrid floorGrid(int widthTiles) {
        CollisionGrid grid = new CollisionGrid(widthTiles, 10, 32);
        for (int x = 0; x < widthTiles; x++) {
            grid.setSolid(x, 0, true);
        }
        return grid;
    }
}
