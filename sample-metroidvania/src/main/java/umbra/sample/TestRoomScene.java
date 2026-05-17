package umbra.sample;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.files.FileHandle;
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
import java.util.List;
import java.util.Objects;

final class TestRoomScene implements Scene {
    private static final int PLAYER_ENTITY_ID = 1;
    private static final int SLIME_ENTITY_ID = 2;
    private static final DebugColor SOLID_TILE_COLOR = new DebugColor(0.18f, 0.30f, 0.22f, 1.0f);
    private static final DebugColor GRID_COLOR = new DebugColor(0.20f, 0.45f, 0.48f, 0.38f);
    private static final DebugColor PLAYER_OUTLINE_COLOR = new DebugColor(1.0f, 1.0f, 0.0f, 1.0f);
    private static final DebugColor HURTBOX_COLOR = new DebugColor(1.0f, 1.0f, 0.0f, 1.0f);
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

    private final LibGdxSpriteBatchRenderer spriteRenderer;
    private final LibGdxDebugShapeRenderer debugRenderer;
    private final DebugGeometryBuilder debugGeometryBuilder = new DebugGeometryBuilder();
    private final Camera camera;
    private final EngineConfig config;
    private final RoomDefinition room;
    private final CollisionGrid grid;
    private final AnimationSetDefinition playerAnimationSet;
    private final AnimationSetDefinition slimeAnimationSet;
    private final AnimationPlayer playerAnimator = new AnimationPlayer();
    private final AnimationPlayer slimeAnimator = new AnimationPlayer();
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

    TestRoomScene(SpriteBatch spriteBatch, ShapeRenderer shapes, Camera camera, EngineConfig config) {
        this.spriteRenderer = new LibGdxSpriteBatchRenderer(spriteBatch);
        this.debugRenderer = new LibGdxDebugShapeRenderer(shapes);
        this.camera = camera;
        this.config = config;
        this.playerAnimationSet = loadAnimationSet("/metadata/player_knight.anim.json");
        this.slimeAnimationSet = loadAnimationSet("/metadata/slime_green.anim.json");
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
        RoomDefinition.SpawnPoint slimeSpawn = findSlimeSpawn();
        this.slime = new KinematicBody(slimeSpawn.x(), slimeSpawn.y(), 28.0f, 18.0f);
        PatrolControllerConfig createdSlimePatrolConfig = PatrolControllerConfig.slimeDefaults();
        this.slimeImpulseConfig = new KinematicImpulseConfig(
                createdSlimePatrolConfig.gravityPixelsPerSecondSquared(),
                createdSlimePatrolConfig.maxFallSpeedPixelsPerSecond()
        );
        this.slimeController = new PatrolController(createdSlimePatrolConfig, -1);
        this.playerAnimator.play(playerAnimationSet.clip("idle"));
        this.slimeAnimator.play(slimeAnimationSet.clip("move"));
    }

    @Override
    public void onExit() {
        spriteRenderer.disposeTextures();
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
            playerAnimator.restart(playerAnimationSet.clip("idle"));
            slimeAnimator.restart(slimeAnimationSet.clip("move"));
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
        updateAnimations(deltaSeconds);
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

    private void updateAnimations(float deltaSeconds) {
        playerAnimator.play(resolvePlayerClip());
        playerAnimator.update(deltaSeconds);

        if (!slimeHealth.defeated()) {
            slimeAnimator.play(slimeAnimationSet.clip("move"));
            slimeAnimator.update(deltaSeconds);
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

    private void drawEnemy() {
        AnimationClipDefinition clip = slimeAnimator.clip();
        int frameIndex = slimeAnimator.frameIndex();
        String textureId = clip == null ? null : clip.textureIdForFrame(frameIndex);
        if (!slimeHealth.defeated() && textureId != null && spriteRenderer.hasTexture(textureId)) {
            float drawWidth = 56.0f;
            float drawHeight = 38.0f;
            SpriteDrawList sprites = new SpriteDrawList();
            sprites.add(new SpriteDrawCommand(
                    textureId,
                    clip.sourceXForFrame(frameIndex),
                    clip.sourceYForFrame(frameIndex),
                    clip.frameWidth(),
                    clip.frameHeight(),
                    slime.x() + slime.width() * 0.5f - drawWidth * 0.5f,
                    slime.y() - 8.0f,
                    drawWidth,
                    drawHeight,
                    slimeController.facingRight(),
                    false,
                    WHITE
            ));
            spriteRenderer.render(sprites);
            return;
        }

        DebugColor enemyColor;
        if (!slimeHealth.defeated()) {
            enemyColor = new DebugColor(0.30f, 0.85f, 0.32f, 1.0f);
        } else {
            enemyColor = new DebugColor(0.15f, 0.18f, 0.15f, 1.0f);
        }
        DebugDrawList drawList = new DebugDrawList();
        drawList.addRect(new DebugRect(slime.x(), slime.y(), slime.width(), slime.height(), enemyColor, DebugShapeStyle.FILLED));
        debugRenderer.render(drawList);
    }

    private void drawCombatDebug() {
        DebugDrawList drawList = new DebugDrawList();
        debugGeometryBuilder.addAabb(drawList, slime.bounds(), HURTBOX_COLOR);

        if (currentAttackHitbox != null) {
            DebugColor hitboxColor = currentAttackHitbox.active() ? ATTACK_ACTIVE_COLOR : ATTACK_INACTIVE_COLOR;
            Aabb hitbox = currentAttackHitbox.bounds();
            debugGeometryBuilder.addAabb(drawList, hitbox, hitboxColor);
        }
        debugRenderer.render(drawList);
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
