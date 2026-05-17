package umbra.sample;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import umbra.combat.AttackDefinition;
import umbra.combat.AttackPhase;
import umbra.combat.AttackTimelineDefinition;
import umbra.combat.AttackTimelinePlayer;
import umbra.combat.CombatResolver;
import umbra.combat.CombatTeam;
import umbra.combat.DamageApplication;
import umbra.combat.DamageEvent;
import umbra.combat.FacingDirection;
import umbra.combat.HealthPool;
import umbra.combat.HitPauseTimer;
import umbra.combat.HitStunTimer;
import umbra.combat.HitboxDefinition;
import umbra.combat.HitboxInstance;
import umbra.combat.HurtboxInstance;
import umbra.core.EngineConfig;
import umbra.core.Scene;
import umbra.physics.Aabb;
import umbra.physics.CollisionGrid;
import umbra.physics.KinematicBody;
import umbra.physics.KinematicImpulseConfig;
import umbra.physics.KinematicImpulseMover;
import umbra.physics.MovementResult;
import umbra.physics.enemy.PatrolController;
import umbra.physics.enemy.PatrolControllerConfig;
import umbra.physics.player.PlayerController;
import umbra.physics.player.PlayerControllerConfig;
import umbra.physics.player.PlayerInput;
import umbra.physics.player.PlayerState;
import umbra.room.RoomDefinition;
import umbra.room.RoomLoader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

final class TestRoomScene implements Scene {
    private static final int PLAYER_ENTITY_ID = 1;
    private static final int SLIME_ENTITY_ID = 2;

    private final ShapeRenderer shapes;
    private final Camera camera;
    private final EngineConfig config;
    private final RoomDefinition room;
    private final CollisionGrid grid;
    private final KinematicBody player;
    private final PlayerController controller;
    private final KinematicImpulseConfig playerImpulseConfig;
    private final KinematicImpulseMover playerKnockbackMover = new KinematicImpulseMover();
    private final KinematicBody slime;
    private final KinematicImpulseConfig slimeImpulseConfig;
    private final PatrolController slimeController;
    private final KinematicImpulseMover enemyKnockbackMover = new KinematicImpulseMover();
    private final CombatResolver combatResolver = new CombatResolver();
    private final AttackTimelinePlayer attackTimeline = new AttackTimelinePlayer();
    private final AttackTimelineDefinition slashTimeline = new AttackTimelineDefinition(
            new AttackDefinition("player_slash_01", 1, 160.0f, 40.0f, 0.045f, 0.18f, "slash"),
            0.06f,
            0.10f,
            0.18f
    );
    private final AttackDefinition slimeContactAttack = new AttackDefinition(
            "slime_contact",
            1,
            120.0f,
            150.0f,
            0.0f,
            0.12f,
            "contact"
    );
    private final HitboxDefinition slashHitboxDefinition = new HitboxDefinition(34.0f, 28.0f, 0.0f, 5.0f);
    private final HealthPool playerHealth = new HealthPool(5, 0.75f);
    private final HealthPool slimeHealth = new HealthPool(3, 0.0f);
    private final HitPauseTimer hitPauseTimer = new HitPauseTimer();
    private final HitStunTimer playerHitStunTimer = new HitStunTimer();
    private final HitStunTimer slimeHitStunTimer = new HitStunTimer();
    private HitboxInstance currentAttackHitbox;
    private boolean facingRight = true;
    private boolean attackFacingRight = true;
    private boolean previousJumpDown;
    private PlayerState state = PlayerState.IDLE;

    TestRoomScene(ShapeRenderer shapes, Camera camera, EngineConfig config) {
        this.shapes = shapes;
        this.camera = camera;
        this.config = config;
        this.room = loadRoom();
        this.grid = createGrid(room);
        RoomDefinition.SpawnPoint playerSpawn = room.spawns().stream()
                .filter(spawn -> spawn.type().equals("player_spawn"))
                .findFirst()
                .orElseThrow();
        this.player = new KinematicBody(playerSpawn.x(), playerSpawn.y(), 18.0f, 38.0f);
        PlayerControllerConfig createdPlayerConfig = PlayerControllerConfig.metroidvaniaDefaults();
        this.playerImpulseConfig = new KinematicImpulseConfig(
                createdPlayerConfig.gravityPixelsPerSecondSquared(),
                createdPlayerConfig.maxFallSpeedPixelsPerSecond()
        );
        this.controller = new PlayerController(createdPlayerConfig);
        RoomDefinition.SpawnPoint slimeSpawn = findSlimeSpawn();
        this.slime = new KinematicBody(slimeSpawn.x(), slimeSpawn.y(), 28.0f, 18.0f);
        PatrolControllerConfig createdSlimePatrolConfig = PatrolControllerConfig.slimeDefaults();
        this.slimeImpulseConfig = new KinematicImpulseConfig(
                createdSlimePatrolConfig.gravityPixelsPerSecondSquared(),
                createdSlimePatrolConfig.maxFallSpeedPixelsPerSecond()
        );
        this.slimeController = new PatrolController(createdSlimePatrolConfig, -1);
    }

    @Override
    public void update(float deltaSeconds) {
        if (hitPauseTimer.paused()) {
            hitPauseTimer.update(deltaSeconds);
            clampCameraToRoom();
            return;
        }

        boolean jumpDown = Gdx.input.isKeyPressed(Input.Keys.SPACE) || Gdx.input.isKeyPressed(Input.Keys.UP);
        PlayerInput input = new PlayerInput(
                Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT),
                Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT),
                jumpDown && !previousJumpDown,
                jumpDown
        );
        if (input.left() && !input.right()) {
            facingRight = false;
        } else if (input.right() && !input.left()) {
            facingRight = true;
        }
        previousJumpDown = jumpDown;

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            RoomDefinition.SpawnPoint playerSpawn = room.spawns().stream()
                    .filter(spawn -> spawn.type().equals("player_spawn"))
                    .findFirst()
                    .orElseThrow();
            player.setPosition(playerSpawn.x(), playerSpawn.y());
            player.setVelocityX(0.0f);
            player.setVelocityY(0.0f);
            RoomDefinition.SpawnPoint slimeSpawn = findSlimeSpawn();
            slime.setPosition(slimeSpawn.x(), slimeSpawn.y());
            slime.setVelocityX(0.0f);
            slime.setVelocityY(0.0f);
            slimeController.reset(-1);
            playerHealth.reset();
            slimeHealth.reset();
            hitPauseTimer.reset();
            playerHitStunTimer.reset();
            slimeHitStunTimer.reset();
            currentAttackHitbox = null;
            attackTimeline.reset();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.J)
                && attackTimeline.acceptingNewAttack()
                && !playerHealth.defeated()
                && !playerHitStunTimer.stunned()) {
            attackFacingRight = facingRight;
            attackTimeline.start(slashTimeline);
            currentAttackHitbox = createPlayerSlashHitbox();
        }
        if (!playerHealth.defeated()) {
            if (playerHitStunTimer.stunned()) {
                updatePlayerHitStun(deltaSeconds);
            } else {
                state = controller.update(input, player, grid, deltaSeconds);
            }
        }
        if (!slimeHealth.defeated()) {
            updateSlimeMovement(deltaSeconds);
        }
        updateCombat(deltaSeconds);
        clampCameraToRoom();
    }

    @Override
    public void render() {
        drawRoom();
        drawEnemy();
        drawPlayer();
        drawCombatDebug();
        drawHud();
    }

    private RoomDefinition loadRoom() {
        try (InputStreamReader reader = new InputStreamReader(
                Objects.requireNonNull(TestRoomScene.class.getResourceAsStream("/rooms/forest_test_01.json")),
                StandardCharsets.UTF_8
        )) {
            return new RoomLoader().load(reader);
        } catch (IOException exception) {
            throw new IllegalStateException("failed to close room resource", exception);
        }
    }

    private CollisionGrid createGrid(RoomDefinition room) {
        CollisionGrid createdGrid = new CollisionGrid(room.widthTiles(), room.heightTiles(), room.tileSize());
        for (RoomDefinition.TileCell tile : room.solidTiles()) {
            createdGrid.setSolid(tile.x(), tile.y(), true);
        }
        return createdGrid;
    }

    private RoomDefinition.SpawnPoint findSlimeSpawn() {
        return room.spawns().stream()
                .filter(spawn -> spawn.id().equals("slime_a"))
                .findFirst()
                .orElse(new RoomDefinition.SpawnPoint("slime_a", "enemy_spawn", 520.0f, 64.0f));
    }

    private HitboxInstance createPlayerSlashHitbox() {
        return slashHitboxDefinition.createInstance(
                PLAYER_ENTITY_ID,
                CombatTeam.PLAYER,
                slashTimeline.attack(),
                player.bounds(),
                attackFacingRight ? FacingDirection.RIGHT : FacingDirection.LEFT,
                false
        );
    }

    private void updateCombat(float deltaSeconds) {
        playerHealth.update(deltaSeconds);
        slimeHealth.update(deltaSeconds);
        attackTimeline.update(deltaSeconds);

        if (currentAttackHitbox != null) {
            currentAttackHitbox.setBounds(slashHitboxDefinition.createBounds(
                    player.bounds(),
                    attackFacingRight ? FacingDirection.RIGHT : FacingDirection.LEFT
            ));
            currentAttackHitbox.setActive(attackTimeline.hitboxActive());
            if (!slimeHealth.defeated()) {
                List<DamageEvent> damageEvents = combatResolver.resolve(
                        List.of(currentAttackHitbox),
                        List.of(new HurtboxInstance(SLIME_ENTITY_ID, CombatTeam.ENEMY, slime.bounds(), true))
                );
                for (DamageEvent event : damageEvents) {
                    DamageApplication application = slimeHealth.apply(event);
                    if (application.applied()) {
                        hitPauseTimer.trigger(event.hitPauseSeconds());
                        slimeHitStunTimer.trigger(event.hitStunSeconds());
                        slime.setVelocityX(event.knockbackX());
                        slime.setVelocityY(event.knockbackY());
                    }
                }
            }

            if (attackTimeline.phase() == AttackPhase.FINISHED) {
                currentAttackHitbox = null;
            }
        }

        if (!playerHealth.defeated() && !slimeHealth.defeated()) {
            List<DamageEvent> damageEvents = combatResolver.resolve(
                    List.of(new HitboxInstance(SLIME_ENTITY_ID, CombatTeam.ENEMY, slimeContactAttack, slime.bounds(), true)),
                    List.of(new HurtboxInstance(PLAYER_ENTITY_ID, CombatTeam.PLAYER, player.bounds(), true))
            );
            for (DamageEvent event : damageEvents) {
                DamageApplication application = playerHealth.apply(event);
                if (application.applied()) {
                    playerHitStunTimer.trigger(event.hitStunSeconds());
                    player.setVelocityX(event.knockbackX());
                    player.setVelocityY(event.knockbackY());
                }
            }
        }
    }

    private void updatePlayerHitStun(float deltaSeconds) {
        MovementResult result = playerKnockbackMover.update(
                player,
                grid,
                playerImpulseConfig,
                deltaSeconds
        );

        if (result.hitGround()) {
            state = Math.abs(player.velocityX()) < 0.01f ? PlayerState.IDLE : PlayerState.RUN;
        } else if (result.hitCeiling()) {
            state = PlayerState.FALL;
        } else {
            state = player.velocityY() > 0.0f ? PlayerState.JUMP : PlayerState.FALL;
        }

        playerHitStunTimer.update(deltaSeconds);
        if (!playerHitStunTimer.stunned()) {
            player.setVelocityX(0.0f);
        }
    }

    private void updateSlimeMovement(float deltaSeconds) {
        if (!slimeHitStunTimer.stunned()) {
            slimeController.update(slime, grid, deltaSeconds);
            return;
        }

        enemyKnockbackMover.update(
                slime,
                grid,
                slimeImpulseConfig,
                deltaSeconds
        );

        slimeHitStunTimer.update(deltaSeconds);
        if (!slimeHitStunTimer.stunned()) {
            slime.setVelocityX(0.0f);
        }
    }

    private void clampCameraToRoom() {
        float roomWidth = grid.widthTiles() * grid.tileSize();
        float roomHeight = grid.heightTiles() * grid.tileSize();
        float halfWidth = config.viewportWidth() / 2.0f;
        float halfHeight = config.viewportHeight() / 2.0f;
        float targetX = Math.max(halfWidth, Math.min(player.x(), roomWidth - halfWidth));
        float targetY = Math.max(halfHeight, Math.min(player.y(), roomHeight - halfHeight));
        camera.position.set(targetX, targetY, 0.0f);
    }

    private void drawRoom() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int y = 0; y < grid.heightTiles(); y++) {
            for (int x = 0; x < grid.widthTiles(); x++) {
                if (grid.isSolidCell(x, y)) {
                    shapes.setColor(0.18f, 0.30f, 0.22f, 1.0f);
                    shapes.rect(x * grid.tileSize(), y * grid.tileSize(), grid.tileSize(), grid.tileSize());
                }
            }
        }
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(0.20f, 0.45f, 0.48f, 0.38f);
        for (int x = 0; x <= grid.widthTiles(); x++) {
            shapes.line(x * grid.tileSize(), 0.0f, x * grid.tileSize(), grid.heightTiles() * grid.tileSize());
        }
        for (int y = 0; y <= grid.heightTiles(); y++) {
            shapes.line(0.0f, y * grid.tileSize(), grid.widthTiles() * grid.tileSize(), y * grid.tileSize());
        }
        shapes.end();
    }

    private void drawPlayer() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(playerColor());
        shapes.rect(player.x(), player.y(), player.width(), player.height());
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.YELLOW);
        shapes.rect(player.x(), player.y(), player.width(), player.height());
        shapes.end();
    }

    private void drawEnemy() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        if (!slimeHealth.defeated()) {
            shapes.setColor(0.30f, 0.85f, 0.32f, 1.0f);
        } else {
            shapes.setColor(0.15f, 0.18f, 0.15f, 1.0f);
        }
        shapes.rect(slime.x(), slime.y(), slime.width(), slime.height());
        shapes.end();
    }

    private void drawCombatDebug() {
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.YELLOW);
        shapes.rect(slime.x(), slime.y(), slime.width(), slime.height());

        if (currentAttackHitbox != null) {
            if (currentAttackHitbox.active()) {
                shapes.setColor(Color.RED);
            } else {
                shapes.setColor(0.7f, 0.15f, 0.15f, 0.55f);
            }
            Aabb hitbox = currentAttackHitbox.bounds();
            shapes.rect(hitbox.x(), hitbox.y(), hitbox.width(), hitbox.height());
        }
        shapes.end();
    }

    private Color playerColor() {
        if (playerHealth.defeated()) {
            return Color.DARK_GRAY;
        }
        if (playerHealth.invulnerable()) {
            return Color.MAGENTA;
        }
        return switch (state) {
            case IDLE -> Color.SKY;
            case RUN -> Color.CYAN;
            case JUMP -> Color.LIME;
            case FALL -> Color.ORANGE;
        };
    }

    private void drawHud() {
        Gdx.graphics.setTitle("Umbra2D | " + room.roomId()
                + " | " + state
                + " | attack=" + attackTimeline.phase()
                + " | hitPause=" + hitPauseTimer.paused()
                + " | playerStun=" + playerHitStunTimer.stunned()
                + " | slimeStun=" + slimeHitStunTimer.stunned()
                + " | playerHP=" + playerHealth.currentHealth()
                + " | slimeHP=" + slimeHealth.currentHealth()
                + " | A/D move, Space jump, J attack, R reset, Esc quit");
    }
}
