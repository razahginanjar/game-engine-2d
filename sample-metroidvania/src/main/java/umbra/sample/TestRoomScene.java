package umbra.sample;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import umbra.animation.AnimationClipDefinition;
import umbra.animation.AnimationMetadataLoader;
import umbra.animation.AnimationPlayer;
import umbra.animation.AnimationSetDefinition;
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
import umbra.render.debug.DebugColor;
import umbra.render.debug.DebugDrawList;
import umbra.render.debug.DebugGeometryBuilder;
import umbra.render.debug.DebugRect;
import umbra.render.debug.DebugShapeStyle;
import umbra.render.debug.LibGdxDebugShapeRenderer;
import umbra.render.sprite.LibGdxSpriteBatchRenderer;
import umbra.render.sprite.SpriteDrawCommand;
import umbra.render.sprite.SpriteDrawList;
import umbra.room.RoomDefinition;
import umbra.room.RoomLoader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class TestRoomScene implements Scene {
    private static final int PLAYER_ENTITY_ID = 1;
    private static final int FIRST_ENEMY_ENTITY_ID = 100;
    private static final DebugColor SOLID_TILE_COLOR = new DebugColor(0.18f, 0.30f, 0.22f, 1.0f);
    private static final DebugColor GRID_COLOR = new DebugColor(0.20f, 0.45f, 0.48f, 0.38f);
    private static final DebugColor PLAYER_OUTLINE_COLOR = new DebugColor(1.0f, 1.0f, 0.0f, 1.0f);
    private static final DebugColor HURTBOX_COLOR = new DebugColor(1.0f, 1.0f, 0.0f, 1.0f);
    private static final DebugColor SELECTED_ENEMY_COLOR = new DebugColor(0.15f, 0.85f, 1.0f, 1.0f);
    private static final DebugColor ATTACK_ACTIVE_COLOR = new DebugColor(1.0f, 0.0f, 0.0f, 1.0f);
    private static final DebugColor ATTACK_INACTIVE_COLOR = new DebugColor(0.7f, 0.15f, 0.15f, 0.55f);
    private static final DebugColor WHITE = new DebugColor(1.0f, 1.0f, 1.0f, 1.0f);
    private static final String PLAYER_IDLE_TEXTURE = "player_idle";
    private static final String PLAYER_RUN_TEXTURE = "player_run";
    private static final String PLAYER_JUMP_TEXTURE = "player_jump";
    private static final String PLAYER_FALL_TEXTURE = "player_fall";
    private static final String PLAYER_ATTACK_TEXTURE = "player_attack";
    private static final String PLAYER_HIT_TEXTURE = "player_hit";
    private static final String PLAYER_DEATH_TEXTURE = "player_death";
    private static final String SLIME_TEXTURE_PREFIX = "slime_green_move_";
    private static final float EDITOR_BUTTON_X = 12.0f;
    private static final float EDITOR_BUTTON_TOP_Y = 12.0f;
    private static final float EDITOR_BUTTON_WIDTH = 132.0f;
    private static final float EDITOR_BUTTON_HEIGHT = 24.0f;
    private static final float EDITOR_BUTTON_GAP = 6.0f;

    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final BitmapFont editorFont = new BitmapFont();
    private final Matrix4 uiProjection = new Matrix4();
    private final LibGdxSpriteBatchRenderer spriteRenderer;
    private final LibGdxDebugShapeRenderer debugRenderer;
    private final DebugGeometryBuilder debugGeometryBuilder = new DebugGeometryBuilder();
    private final Camera camera;
    private final EngineConfig config;
    private final RoomDefinition room;
    private final CollisionGrid grid;
    private final AnimationSetDefinition playerAnimationSet;
    private final AnimationSetDefinition slimeAnimationSet;
    private final AnimationSetDefinition goblinAnimationSet;
    private final AnimationSetDefinition flyingEyeAnimationSet;
    private final AnimationSetDefinition skeletonAnimationSet;
    private final AnimationSetDefinition mushroomAnimationSet;
    private final AnimationPlayer playerAnimator = new AnimationPlayer();
    private final KinematicBody player;
    private final PlayerController controller;
    private final KinematicImpulseConfig playerImpulseConfig;
    private final KinematicImpulseMover playerKnockbackMover = new KinematicImpulseMover();
    private final List<EnemyActor> enemies = new ArrayList<>();
    private final CombatResolver combatResolver = new CombatResolver();
    private final AttackTimelinePlayer attackTimeline = new AttackTimelinePlayer();
    private final AttackTimelineDefinition slashTimeline = new AttackTimelineDefinition(
            new AttackDefinition("player_slash_01", 1, 160.0f, 40.0f, 0.045f, 0.18f, "slash"),
            0.06f,
            0.10f,
            0.18f
    );
    private final AttackDefinition enemyContactAttack = new AttackDefinition(
            "enemy_contact",
            1,
            120.0f,
            150.0f,
            0.0f,
            0.12f,
            "contact"
    );
    private final HitboxDefinition slashHitboxDefinition = new HitboxDefinition(34.0f, 28.0f, 0.0f, 5.0f);
    private final HealthPool playerHealth = new HealthPool(5, 0.75f);
    private final HitPauseTimer hitPauseTimer = new HitPauseTimer();
    private final HitStunTimer playerHitStunTimer = new HitStunTimer();
    private HitboxInstance currentAttackHitbox;
    private boolean facingRight = true;
    private boolean attackFacingRight = true;
    private boolean previousJumpDown;
    private int nextEnemyEntityId = FIRST_ENEMY_ENTITY_ID;
    private EnemyActor selectedEnemy;
    private PlayerState state = PlayerState.IDLE;

    TestRoomScene(SpriteBatch spriteBatch, ShapeRenderer shapes, Camera camera, EngineConfig config) {
        this.batch = spriteBatch;
        this.shapes = shapes;
        this.spriteRenderer = new LibGdxSpriteBatchRenderer(spriteBatch);
        this.debugRenderer = new LibGdxDebugShapeRenderer(shapes);
        this.camera = camera;
        this.config = config;
        this.playerAnimationSet = loadAnimationSet("/metadata/player_knight.anim.json");
        this.slimeAnimationSet = loadAnimationSet("/metadata/slime_green.anim.json");
        this.goblinAnimationSet = loadAnimationSet("/metadata/goblin.anim.json");
        this.flyingEyeAnimationSet = loadAnimationSet("/metadata/flying_eye.anim.json");
        this.skeletonAnimationSet = loadAnimationSet("/metadata/skeleton.anim.json");
        this.mushroomAnimationSet = loadAnimationSet("/metadata/mushroom.anim.json");
        loadExternalSprites();
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
        createEnemies();
        this.playerAnimator.play(playerAnimationSet.clip("idle"));
    }

    @Override
    public void onExit() {
        spriteRenderer.disposeTextures();
        editorFont.dispose();
    }

    @Override
    public void update(float deltaSeconds) {
        handleEditorInput();
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
            for (EnemyActor enemy : enemies) {
                enemy.reset();
            }
            selectedEnemy = null;
            playerHealth.reset();
            hitPauseTimer.reset();
            playerHitStunTimer.reset();
            currentAttackHitbox = null;
            attackTimeline.reset();
            playerAnimator.restart(playerAnimationSet.clip("idle"));
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
        updateEnemies(deltaSeconds);
        updateCombat(deltaSeconds);
        updateAnimations(deltaSeconds);
        clampCameraToRoom();
    }

    @Override
    public void render() {
        drawRoom();
        drawEnemies();
        drawPlayer();
        drawCombatDebug();
        drawEditorOverlay();
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

    private RoomDefinition.SpawnPoint findSpawn(String spawnId, float fallbackX, float fallbackY) {
        return room.spawns().stream()
                .filter(spawn -> spawn.id().equals(spawnId))
                .findFirst()
                .orElse(new RoomDefinition.SpawnPoint(spawnId, "enemy_spawn", fallbackX, fallbackY));
    }

    private void handleEditorInput() {
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            return;
        }

        float screenX = Gdx.input.getX();
        float screenY = Gdx.graphics.getHeight() - Gdx.input.getY();
        EditorButtonAction action = editorButtonActionAt(screenX, screenY);
        if (action != null) {
            executeEditorAction(action);
            return;
        }

        Vector3 worldClick = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0.0f));
        selectedEnemy = enemyAt(worldClick.x, worldClick.y);
    }

    private EditorButtonAction editorButtonActionAt(float screenX, float screenY) {
        int row = 0;
        for (EnemyKind kind : EnemyKind.values()) {
            if (insideEditorButton(row, screenX, screenY)) {
                return new EditorButtonAction(EditorAction.ADD, kind);
            }
            row++;
        }
        if (insideEditorButton(row, screenX, screenY)) {
            return new EditorButtonAction(EditorAction.REMOVE_SELECTED, null);
        }
        return null;
    }

    private boolean insideEditorButton(int row, float screenX, float screenY) {
        float y = editorButtonY(row);
        return screenX >= EDITOR_BUTTON_X
                && screenX <= EDITOR_BUTTON_X + EDITOR_BUTTON_WIDTH
                && screenY >= y
                && screenY <= y + EDITOR_BUTTON_HEIGHT;
    }

    private float editorButtonY(int row) {
        return Gdx.graphics.getHeight()
                - EDITOR_BUTTON_TOP_Y
                - EDITOR_BUTTON_HEIGHT
                - row * (EDITOR_BUTTON_HEIGHT + EDITOR_BUTTON_GAP);
    }

    private void executeEditorAction(EditorButtonAction action) {
        if (action.action == EditorAction.ADD) {
            spawnEnemyFromEditor(action.enemyKind);
            return;
        }
        if (selectedEnemy != null) {
            enemies.remove(selectedEnemy);
            selectedEnemy = null;
        }
    }

    private void spawnEnemyFromEditor(EnemyKind kind) {
        float roomWidth = grid.widthTiles() * grid.tileSize();
        float roomHeight = grid.heightTiles() * grid.tileSize();
        float spawnX = Math.max(48.0f, Math.min(player.x() + 96.0f, roomWidth - 96.0f));
        float spawnY = kind == EnemyKind.FLYING_EYE
                ? Math.max(96.0f, Math.min(player.y() + 120.0f, roomHeight - 128.0f))
                : Math.max(40.0f, player.y());
        EnemyActor enemy = createEnemy(kind, kind.spawnPrefix + "_" + nextEnemyEntityId, spawnX, spawnY);
        enemies.add(enemy);
        selectedEnemy = enemy;
    }

    private EnemyActor enemyAt(float worldX, float worldY) {
        for (int index = enemies.size() - 1; index >= 0; index--) {
            EnemyActor enemy = enemies.get(index);
            Aabb bounds = enemy.body.bounds();
            if (!enemy.health.defeated()
                    && worldX >= bounds.x()
                    && worldX <= bounds.right()
                    && worldY >= bounds.y()
                    && worldY <= bounds.top()) {
                return enemy;
            }
        }
        return null;
    }

    private void createEnemies() {
        RoomDefinition.SpawnPoint slimeSpawn = findSpawn("slime_a", 520.0f, 64.0f);
        enemies.add(createEnemy(EnemyKind.SLIME, slimeSpawn.id(), slimeSpawn.x(), slimeSpawn.y()));
        RoomDefinition.SpawnPoint goblinSpawn = findSpawn("goblin_a", 690.0f, 64.0f);
        enemies.add(createEnemy(EnemyKind.GOBLIN, goblinSpawn.id(), goblinSpawn.x(), goblinSpawn.y()));
        RoomDefinition.SpawnPoint flyingEyeSpawn = findSpawn("flying_eye_a", 850.0f, 220.0f);
        enemies.add(createEnemy(EnemyKind.FLYING_EYE, flyingEyeSpawn.id(), flyingEyeSpawn.x(), flyingEyeSpawn.y()));
        RoomDefinition.SpawnPoint skeletonSpawn = findSpawn("skeleton_a", 940.0f, 64.0f);
        enemies.add(createEnemy(EnemyKind.SKELETON, skeletonSpawn.id(), skeletonSpawn.x(), skeletonSpawn.y()));
        RoomDefinition.SpawnPoint mushroomSpawn = findSpawn("mushroom_a", 1100.0f, 64.0f);
        enemies.add(createEnemy(EnemyKind.MUSHROOM, mushroomSpawn.id(), mushroomSpawn.x(), mushroomSpawn.y()));
    }

    private EnemyActor createEnemy(EnemyKind kind, String spawnId, float x, float y) {
        RoomDefinition.SpawnPoint spawn = new RoomDefinition.SpawnPoint(spawnId, "enemy_spawn", x, y);
        return switch (kind) {
            case SLIME -> createGroundEnemy(
                spawn,
                slimeAnimationSet,
                28.0f,
                18.0f,
                56.0f,
                38.0f,
                0.0f,
                -8.0f,
                new DebugColor(0.30f, 0.85f, 0.32f, 1.0f),
                PatrolControllerConfig.slimeDefaults()
            );
            case GOBLIN -> createGroundEnemy(
                spawn,
                goblinAnimationSet,
                26.0f,
                42.0f,
                78.0f,
                78.0f,
                0.0f,
                -18.0f,
                new DebugColor(0.35f, 0.75f, 0.28f, 1.0f),
                new PatrolControllerConfig(70.0f, 1200.0f, 420.0f)
            );
            case FLYING_EYE -> createFlyingEnemy(
                spawn,
                flyingEyeAnimationSet,
                30.0f,
                26.0f,
                72.0f,
                72.0f,
                0.0f,
                -22.0f,
                new DebugColor(0.90f, 0.25f, 0.55f, 1.0f),
                64.0f
            );
            case SKELETON -> createGroundEnemy(
                spawn,
                skeletonAnimationSet,
                24.0f,
                46.0f,
                82.0f,
                82.0f,
                0.0f,
                -22.0f,
                new DebugColor(0.82f, 0.82f, 0.70f, 1.0f),
                new PatrolControllerConfig(48.0f, 1200.0f, 420.0f)
            );
            case MUSHROOM -> createGroundEnemy(
                spawn,
                mushroomAnimationSet,
                30.0f,
                34.0f,
                74.0f,
                74.0f,
                0.0f,
                -18.0f,
                new DebugColor(0.88f, 0.33f, 0.34f, 1.0f),
                new PatrolControllerConfig(42.0f, 1200.0f, 420.0f)
            );
        };
    }

    private EnemyActor createGroundEnemy(
            RoomDefinition.SpawnPoint spawn,
            AnimationSetDefinition animationSet,
            float bodyWidth,
            float bodyHeight,
            float drawWidth,
            float drawHeight,
            float drawOffsetX,
            float drawOffsetY,
            DebugColor fallbackColor,
            PatrolControllerConfig patrolConfig
    ) {
        return new EnemyActor(
                nextEnemyEntityId++,
                spawn.id(),
                EnemyMovement.GROUND_PATROL,
                spawn.x(),
                spawn.y(),
                new KinematicBody(spawn.x(), spawn.y(), bodyWidth, bodyHeight),
                animationSet,
                new PatrolController(patrolConfig, -1),
                new KinematicImpulseConfig(patrolConfig.gravityPixelsPerSecondSquared(), patrolConfig.maxFallSpeedPixelsPerSecond()),
                0.0f,
                drawWidth,
                drawHeight,
                drawOffsetX,
                drawOffsetY,
                fallbackColor
        );
    }

    private EnemyActor createFlyingEnemy(
            RoomDefinition.SpawnPoint spawn,
            AnimationSetDefinition animationSet,
            float bodyWidth,
            float bodyHeight,
            float drawWidth,
            float drawHeight,
            float drawOffsetX,
            float drawOffsetY,
            DebugColor fallbackColor,
            float speed
    ) {
        return new EnemyActor(
                nextEnemyEntityId++,
                spawn.id(),
                EnemyMovement.FLYING_PATROL,
                spawn.x(),
                spawn.y(),
                new KinematicBody(spawn.x(), spawn.y(), bodyWidth, bodyHeight),
                animationSet,
                null,
                new KinematicImpulseConfig(800.0f, 360.0f),
                speed,
                drawWidth,
                drawHeight,
                drawOffsetX,
                drawOffsetY,
                fallbackColor
        );
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
        for (EnemyActor enemy : enemies) {
            enemy.health.update(deltaSeconds);
        }
        attackTimeline.update(deltaSeconds);

        if (currentAttackHitbox != null) {
            currentAttackHitbox.setBounds(slashHitboxDefinition.createBounds(
                    player.bounds(),
                    attackFacingRight ? FacingDirection.RIGHT : FacingDirection.LEFT
            ));
            currentAttackHitbox.setActive(attackTimeline.hitboxActive());
            List<HurtboxInstance> enemyHurtboxes = new ArrayList<>();
            for (EnemyActor enemy : enemies) {
                if (!enemy.health.defeated()) {
                    enemyHurtboxes.add(new HurtboxInstance(enemy.entityId, CombatTeam.ENEMY, enemy.body.bounds(), true));
                }
            }
            List<DamageEvent> damageEvents = combatResolver.resolve(List.of(currentAttackHitbox), enemyHurtboxes);
            for (DamageEvent event : damageEvents) {
                EnemyActor enemy = findEnemy(event.targetEntityId());
                if (enemy == null) {
                    continue;
                }
                DamageApplication application = enemy.health.apply(event);
                if (application.applied()) {
                    hitPauseTimer.trigger(event.hitPauseSeconds());
                    enemy.hitStunTimer.trigger(event.hitStunSeconds());
                    enemy.body.setVelocityX(event.knockbackX());
                    enemy.body.setVelocityY(event.knockbackY());
                }
            }

            if (attackTimeline.phase() == AttackPhase.FINISHED) {
                currentAttackHitbox = null;
            }
        }

        if (!playerHealth.defeated()) {
            List<HitboxInstance> enemyContactHitboxes = new ArrayList<>();
            for (EnemyActor enemy : enemies) {
                if (!enemy.health.defeated()) {
                    enemyContactHitboxes.add(new HitboxInstance(enemy.entityId, CombatTeam.ENEMY, enemyContactAttack, enemy.body.bounds(), true));
                }
            }
            List<DamageEvent> damageEvents = combatResolver.resolve(
                    enemyContactHitboxes,
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

    private EnemyActor findEnemy(int entityId) {
        for (EnemyActor enemy : enemies) {
            if (enemy.entityId == entityId) {
                return enemy;
            }
        }
        return null;
    }

    private AnimationSetDefinition loadAnimationSet(String resourcePath) {
        try (InputStreamReader reader = new InputStreamReader(
                Objects.requireNonNull(TestRoomScene.class.getResourceAsStream(resourcePath)),
                StandardCharsets.UTF_8
        )) {
            return new AnimationMetadataLoader().load(reader);
        } catch (IOException exception) {
            throw new IllegalStateException("failed to close animation resource: " + resourcePath, exception);
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

    private void updateEnemies(float deltaSeconds) {
        for (EnemyActor enemy : enemies) {
            if (enemy.health.defeated()) {
                continue;
            }
            if (enemy.hitStunTimer.stunned()) {
                enemy.knockbackMover.update(enemy.body, grid, enemy.impulseConfig, deltaSeconds);
                enemy.hitStunTimer.update(deltaSeconds);
                if (!enemy.hitStunTimer.stunned()) {
                    enemy.body.setVelocityX(0.0f);
                }
                continue;
            }

            if (enemy.movement == EnemyMovement.GROUND_PATROL) {
                enemy.patrolController.update(enemy.body, grid, deltaSeconds);
            } else {
                updateFlyingEnemy(enemy, deltaSeconds);
            }
        }
    }

    private void updateFlyingEnemy(EnemyActor enemy, float deltaSeconds) {
        enemy.flightAgeSeconds += deltaSeconds;
        float patrolHalfWidth = 96.0f;
        float nextX = enemy.body.x() + enemy.flightDirection * enemy.flightSpeed * deltaSeconds;
        if (nextX < enemy.spawnX - patrolHalfWidth || nextX > enemy.spawnX + patrolHalfWidth) {
            enemy.flightDirection *= -1;
            nextX = enemy.body.x() + enemy.flightDirection * enemy.flightSpeed * deltaSeconds;
        }
        float nextY = enemy.spawnY + (float) Math.sin(enemy.flightAgeSeconds * 3.0f) * 8.0f;
        enemy.body.setPosition(nextX, nextY);
    }

    private void updateAnimations(float deltaSeconds) {
        playerAnimator.play(resolvePlayerClip());
        playerAnimator.update(deltaSeconds);

        for (EnemyActor enemy : enemies) {
            if (!enemy.health.defeated()) {
                enemy.animator.play(enemy.animationSet.clip("move"));
                enemy.animator.update(deltaSeconds);
            }
        }
    }

    private AnimationClipDefinition resolvePlayerClip() {
        if (playerHealth.defeated()) {
            return playerAnimationSet.clip("death");
        }
        if (playerHitStunTimer.stunned()) {
            return playerAnimationSet.clip("hit");
        }
        if (!attackTimeline.acceptingNewAttack()) {
            return playerAnimationSet.clip("attack");
        }
        return switch (state) {
            case IDLE -> playerAnimationSet.clip("idle");
            case RUN -> playerAnimationSet.clip("run");
            case JUMP -> playerAnimationSet.clip("jump");
            case FALL -> playerAnimationSet.clip("fall");
        };
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
        DebugDrawList drawList = new DebugDrawList();
        debugGeometryBuilder.addSolidTiles(drawList, grid, SOLID_TILE_COLOR);
        debugGeometryBuilder.addTileGrid(drawList, grid, GRID_COLOR);
        debugRenderer.render(drawList);
    }

    private void drawPlayer() {
        AnimationClipDefinition clip = playerAnimator.clip();
        int frameIndex = playerAnimator.frameIndex();
        String textureId = clip == null ? null : clip.textureIdForFrame(frameIndex);
        if (textureId != null && spriteRenderer.hasTexture(textureId)) {
            SpriteDrawList sprites = new SpriteDrawList();
            sprites.add(new SpriteDrawCommand(
                    textureId,
                    clip.sourceXForFrame(frameIndex),
                    clip.sourceYForFrame(frameIndex),
                    clip.frameWidth(),
                    clip.frameHeight(),
                    player.x() - 51.0f,
                    player.y() - 30.0f,
                    clip.frameWidth(),
                    clip.frameHeight(),
                    !facingRight,
                    false,
                    playerSpriteTint()
            ));
            spriteRenderer.render(sprites);
        } else {
            drawPlayerFallback();
        }

        DebugDrawList debug = new DebugDrawList();
        debugGeometryBuilder.addAabb(debug, player.bounds(), PLAYER_OUTLINE_COLOR);
        debugRenderer.render(debug);
    }

    private void drawPlayerFallback() {
        DebugDrawList drawList = new DebugDrawList();
        drawList.addRect(new DebugRect(player.x(), player.y(), player.width(), player.height(), playerColor(), DebugShapeStyle.FILLED));
        debugRenderer.render(drawList);
    }

    private void drawEnemies() {
        for (EnemyActor enemy : enemies) {
            if (drawEnemySprite(enemy)) {
                continue;
            }
            DebugColor enemyColor = enemy.health.defeated()
                    ? new DebugColor(0.15f, 0.18f, 0.15f, 1.0f)
                    : enemy.fallbackColor;
            DebugDrawList drawList = new DebugDrawList();
            drawList.addRect(new DebugRect(enemy.body.x(), enemy.body.y(), enemy.body.width(), enemy.body.height(), enemyColor, DebugShapeStyle.FILLED));
            debugRenderer.render(drawList);
        }
    }

    private boolean drawEnemySprite(EnemyActor enemy) {
        AnimationClipDefinition clip = enemy.animator.clip();
        int frameIndex = enemy.animator.frameIndex();
        String textureId = clip == null ? null : clip.textureIdForFrame(frameIndex);
        if (enemy.health.defeated() || textureId == null || !spriteRenderer.hasTexture(textureId)) {
            return false;
        }

        SpriteDrawList sprites = new SpriteDrawList();
        sprites.add(new SpriteDrawCommand(
                textureId,
                clip.sourceXForFrame(frameIndex),
                clip.sourceYForFrame(frameIndex),
                clip.frameWidth(),
                clip.frameHeight(),
                enemy.body.x() + enemy.body.width() * 0.5f - enemy.drawWidth * 0.5f + enemy.drawOffsetX,
                enemy.body.y() + enemy.drawOffsetY,
                enemy.drawWidth,
                enemy.drawHeight,
                !enemy.facingRight(),
                false,
                WHITE
        ));
        spriteRenderer.render(sprites);
        return true;
    }

    private void drawCombatDebug() {
        DebugDrawList drawList = new DebugDrawList();
        for (EnemyActor enemy : enemies) {
            debugGeometryBuilder.addAabb(drawList, enemy.body.bounds(), HURTBOX_COLOR);
        }
        if (selectedEnemy != null && enemies.contains(selectedEnemy)) {
            debugGeometryBuilder.addAabb(drawList, selectedEnemy.body.bounds(), SELECTED_ENEMY_COLOR);
        }

        if (currentAttackHitbox != null) {
            DebugColor hitboxColor = currentAttackHitbox.active() ? ATTACK_ACTIVE_COLOR : ATTACK_INACTIVE_COLOR;
            Aabb hitbox = currentAttackHitbox.bounds();
            debugGeometryBuilder.addAabb(drawList, hitbox, hitboxColor);
        }
        debugRenderer.render(drawList);
    }

    private void drawEditorOverlay() {
        uiProjection.setToOrtho2D(0.0f, 0.0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapes.setProjectionMatrix(uiProjection);
        batch.setProjectionMatrix(uiProjection);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        int row = 0;
        for (EnemyKind kind : EnemyKind.values()) {
            drawEditorButtonBackground(row, true);
            row++;
        }
        drawEditorButtonBackground(row, selectedEnemy != null && enemies.contains(selectedEnemy));
        shapes.end();

        batch.begin();
        editorFont.setColor(Color.WHITE);
        row = 0;
        for (EnemyKind kind : EnemyKind.values()) {
            drawEditorButtonText(row, "Add " + kind.label);
            row++;
        }
        editorFont.setColor(selectedEnemy == null ? Color.LIGHT_GRAY : Color.WHITE);
        drawEditorButtonText(row, "Remove Selected");
        editorFont.setColor(Color.WHITE);
        float helpY = editorButtonY(row + 1) + 16.0f;
        editorFont.draw(batch, "Click an enemy to select it. New enemies spawn near player.", EDITOR_BUTTON_X, helpY);
        if (selectedEnemy != null && enemies.contains(selectedEnemy)) {
            editorFont.draw(batch, "Selected: " + selectedEnemy.spawnId + " #" + selectedEnemy.entityId,
                    EDITOR_BUTTON_X,
                    helpY - 18.0f);
        }
        batch.end();
    }

    private void drawEditorButtonBackground(int row, boolean enabled) {
        float y = editorButtonY(row);
        shapes.setColor(enabled ? new Color(0.10f, 0.16f, 0.20f, 0.92f) : new Color(0.08f, 0.08f, 0.08f, 0.70f));
        shapes.rect(EDITOR_BUTTON_X, y, EDITOR_BUTTON_WIDTH, EDITOR_BUTTON_HEIGHT);
        shapes.setColor(new Color(0.25f, 0.55f, 0.62f, 0.95f));
        shapes.rect(EDITOR_BUTTON_X, y, EDITOR_BUTTON_WIDTH, 2.0f);
    }

    private void drawEditorButtonText(int row, String text) {
        editorFont.draw(batch, text, EDITOR_BUTTON_X + 8.0f, editorButtonY(row) + 17.0f);
    }

    private DebugColor playerColor() {
        if (playerHealth.defeated()) {
            return new DebugColor(0.25f, 0.25f, 0.25f, 1.0f);
        }
        if (playerHealth.invulnerable()) {
            return new DebugColor(1.0f, 0.0f, 1.0f, 1.0f);
        }
        return switch (state) {
            case IDLE -> new DebugColor(0.53f, 0.81f, 0.92f, 1.0f);
            case RUN -> new DebugColor(0.0f, 1.0f, 1.0f, 1.0f);
            case JUMP -> new DebugColor(0.0f, 1.0f, 0.0f, 1.0f);
            case FALL -> new DebugColor(1.0f, 0.65f, 0.0f, 1.0f);
        };
    }

    private DebugColor playerSpriteTint() {
        if (playerHealth.defeated()) {
            return new DebugColor(0.45f, 0.45f, 0.45f, 1.0f);
        }
        if (playerHealth.invulnerable()) {
            return new DebugColor(1.0f, 0.55f, 1.0f, 1.0f);
        }
        return WHITE;
    }

    private void loadExternalSprites() {
        Path assetRoot = Path.of(System.getProperty("umbra.assets.root", "../assets")).toAbsolutePath().normalize();
        registerTextureIfPresent(PLAYER_IDLE_TEXTURE, assetRoot.resolve(Path.of(
                "char",
                "FreeKnight_v1",
                "Colour1",
                "NoOutline",
                "120x80_PNGSheets",
                "_Idle.png"
        )));
        registerTextureIfPresent(PLAYER_RUN_TEXTURE, playerSheetPath(assetRoot, "_Run.png"));
        registerTextureIfPresent(PLAYER_JUMP_TEXTURE, playerSheetPath(assetRoot, "_Jump.png"));
        registerTextureIfPresent(PLAYER_FALL_TEXTURE, playerSheetPath(assetRoot, "_Fall.png"));
        registerTextureIfPresent(PLAYER_ATTACK_TEXTURE, playerSheetPath(assetRoot, "_Attack.png"));
        registerTextureIfPresent(PLAYER_HIT_TEXTURE, playerSheetPath(assetRoot, "_Hit.png"));
        registerTextureIfPresent(PLAYER_DEATH_TEXTURE, playerSheetPath(assetRoot, "_Death.png"));

        for (int frame = 0; frame < 30; frame++) {
            String frameId = SLIME_TEXTURE_PREFIX + String.format("%05d", frame);
            registerTextureIfPresent(frameId, assetRoot.resolve(Path.of(
                    "monster",
                    "Slimes",
                    "SlimeGreen",
                    "SlimeBasic_" + String.format("%05d", frame) + ".png"
            )));
        }
        registerFantasyMonsterSheet(assetRoot, "goblin_move", "Goblin", "Run-sheet.png");
        registerFantasyMonsterSheet(assetRoot, "flying_eye_move", "Flying eye", "Flight-sheet.png");
        registerFantasyMonsterSheet(assetRoot, "skeleton_move", "Skeleton", "Walk-sheet.png");
        registerFantasyMonsterSheet(assetRoot, "mushroom_move", "Mushroom", "Run-sheet.png");
    }

    private Path playerSheetPath(Path assetRoot, String fileName) {
        return assetRoot.resolve(Path.of(
                "char",
                "FreeKnight_v1",
                "Colour1",
                "NoOutline",
                "120x80_PNGSheets",
                fileName
        ));
    }

    private void registerTextureIfPresent(String textureId, Path texturePath) {
        FileHandle file = Gdx.files.absolute(texturePath.toString());
        if (file.exists()) {
            spriteRenderer.registerTexture(textureId, new Texture(file));
        }
    }

    private void registerFantasyMonsterSheet(Path assetRoot, String textureId, String creatureFolder, String fileName) {
        registerTextureIfPresent(textureId, assetRoot.resolve(Path.of(
                "monster",
                "Monsters_Creatures_Fantasy",
                "Monsters_Creatures_Fantasy",
                creatureFolder,
                fileName
        )));
    }

    private enum EnemyKind {
        SLIME("Slime", "slime"),
        GOBLIN("Goblin", "goblin"),
        FLYING_EYE("Flying Eye", "flying_eye"),
        SKELETON("Skeleton", "skeleton"),
        MUSHROOM("Mushroom", "mushroom");

        private final String label;
        private final String spawnPrefix;

        EnemyKind(String label, String spawnPrefix) {
            this.label = label;
            this.spawnPrefix = spawnPrefix;
        }
    }

    private enum EditorAction {
        ADD,
        REMOVE_SELECTED
    }

    private record EditorButtonAction(EditorAction action, EnemyKind enemyKind) {
    }

    private enum EnemyMovement {
        GROUND_PATROL,
        FLYING_PATROL
    }

    private static final class EnemyActor {
        private final int entityId;
        private final String spawnId;
        private final EnemyMovement movement;
        private final float spawnX;
        private final float spawnY;
        private final KinematicBody body;
        private final AnimationSetDefinition animationSet;
        private final AnimationPlayer animator = new AnimationPlayer();
        private final PatrolController patrolController;
        private final KinematicImpulseMover knockbackMover = new KinematicImpulseMover();
        private final KinematicImpulseConfig impulseConfig;
        private final HealthPool health = new HealthPool(3, 0.0f);
        private final HitStunTimer hitStunTimer = new HitStunTimer();
        private final float flightSpeed;
        private final float drawWidth;
        private final float drawHeight;
        private final float drawOffsetX;
        private final float drawOffsetY;
        private final DebugColor fallbackColor;
        private int flightDirection = -1;
        private float flightAgeSeconds;

        private EnemyActor(
                int entityId,
                String spawnId,
                EnemyMovement movement,
                float spawnX,
                float spawnY,
                KinematicBody body,
                AnimationSetDefinition animationSet,
                PatrolController patrolController,
                KinematicImpulseConfig impulseConfig,
                float flightSpeed,
                float drawWidth,
                float drawHeight,
                float drawOffsetX,
                float drawOffsetY,
                DebugColor fallbackColor
        ) {
            this.entityId = entityId;
            this.spawnId = spawnId;
            this.movement = movement;
            this.spawnX = spawnX;
            this.spawnY = spawnY;
            this.body = body;
            this.animationSet = animationSet;
            this.patrolController = patrolController;
            this.impulseConfig = impulseConfig;
            this.flightSpeed = flightSpeed;
            this.drawWidth = drawWidth;
            this.drawHeight = drawHeight;
            this.drawOffsetX = drawOffsetX;
            this.drawOffsetY = drawOffsetY;
            this.fallbackColor = fallbackColor;
            this.animator.play(animationSet.clip("move"));
        }

        private void reset() {
            body.setPosition(spawnX, spawnY);
            body.setVelocityX(0.0f);
            body.setVelocityY(0.0f);
            health.reset();
            hitStunTimer.reset();
            animator.restart(animationSet.clip("move"));
            flightDirection = -1;
            flightAgeSeconds = 0.0f;
            if (patrolController != null) {
                patrolController.reset(-1);
            }
        }

        private boolean facingRight() {
            if (movement == EnemyMovement.FLYING_PATROL) {
                return flightDirection > 0;
            }
            return patrolController != null && patrolController.facingRight();
        }
    }

    private void drawHud() {
        Gdx.graphics.setTitle("Umbra2D | " + room.roomId()
                + " | " + state
                + " | attack=" + attackTimeline.phase()
                + " | hitPause=" + hitPauseTimer.paused()
                + " | playerStun=" + playerHitStunTimer.stunned()
                + " | playerHP=" + playerHealth.currentHealth()
                + " | enemiesAlive=" + aliveEnemyCount()
                + " | A/D move, Space jump, J attack, R reset, Esc quit");
    }

    private int aliveEnemyCount() {
        int count = 0;
        for (EnemyActor enemy : enemies) {
            if (!enemy.health.defeated()) {
                count++;
            }
        }
        return count;
    }
}
