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
import umbra.animation.AnimationSetValidator;
import umbra.ai.EnemyAiState;
import umbra.ai.EnemyBrain;
import umbra.ai.EnemyBrainConfig;
import umbra.ai.EnemyBrainDecision;
import umbra.ai.EnemyBrainInput;
import umbra.boss.BossArenaController;
import umbra.boss.BossArenaDefinition;
import umbra.boss.BossArenaInput;
import umbra.boss.BossArenaStatus;
import umbra.boss.BossAttackPattern;
import umbra.boss.BossAttackSelector;
import umbra.boss.BossFightState;
import umbra.boss.BossPhaseDefinition;
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
import umbra.physics.KinematicMover;
import umbra.physics.MovementResult;
import umbra.physics.enemy.PatrolController;
import umbra.physics.enemy.PatrolControllerConfig;
import umbra.physics.player.PlayerController;
import umbra.physics.player.PlayerControllerConfig;
import umbra.physics.player.PlayerInput;
import umbra.physics.player.PlayerState;
import umbra.progression.AbilityGateBlock;
import umbra.progression.AbilityState;
import umbra.progression.AbilityTriggerResolver;
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
import umbra.room.RoomRegistry;
import umbra.room.CheckpointState;
import umbra.room.RoomTransitionRequest;
import umbra.room.RoomTriggerResolver;
import umbra.save.SaveGame;
import umbra.save.SaveGameCodec;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

final class TestRoomScene implements Scene {
    private static final int PLAYER_ENTITY_ID = 1;
    private static final int FIRST_ENEMY_ENTITY_ID = 100;
    private static final int BOSS_ENTITY_ID = 5000;
    private static final DebugColor SOLID_TILE_COLOR = new DebugColor(0.18f, 0.30f, 0.22f, 1.0f);
    private static final DebugColor GRID_COLOR = new DebugColor(0.20f, 0.45f, 0.48f, 0.38f);
    private static final DebugColor DOOR_TRIGGER_COLOR = new DebugColor(0.10f, 0.85f, 1.0f, 0.85f);
    private static final DebugColor CAMERA_ZONE_COLOR = new DebugColor(0.95f, 0.80f, 0.20f, 0.55f);
    private static final DebugColor CHECKPOINT_COLOR = new DebugColor(0.25f, 1.0f, 0.55f, 0.85f);
    private static final DebugColor ABILITY_PICKUP_COLOR = new DebugColor(0.25f, 0.75f, 1.0f, 0.90f);
    private static final DebugColor ABILITY_GATE_LOCKED_COLOR = new DebugColor(1.0f, 0.20f, 0.12f, 0.85f);
    private static final DebugColor ABILITY_GATE_UNLOCKED_COLOR = new DebugColor(0.25f, 1.0f, 0.45f, 0.35f);
    private static final DebugColor ENEMY_VISION_COLOR = new DebugColor(0.45f, 0.70f, 1.0f, 0.40f);
    private static final DebugColor ENEMY_ATTACK_RANGE_COLOR = new DebugColor(1.0f, 0.45f, 0.15f, 0.70f);
    private static final DebugColor BOSS_ARENA_COLOR = new DebugColor(0.95f, 0.25f, 0.95f, 0.40f);
    private static final DebugColor BOSS_LOCKED_DOOR_COLOR = new DebugColor(1.0f, 0.10f, 0.10f, 0.92f);
    private static final DebugColor BOSS_HURTBOX_COLOR = new DebugColor(1.0f, 0.65f, 0.15f, 1.0f);
    private static final DebugColor PLAYER_OUTLINE_COLOR = new DebugColor(1.0f, 1.0f, 0.0f, 1.0f);
    private static final DebugColor HURTBOX_COLOR = new DebugColor(1.0f, 1.0f, 0.0f, 1.0f);
    private static final DebugColor SELECTED_ENEMY_COLOR = new DebugColor(0.15f, 0.85f, 1.0f, 1.0f);
    private static final DebugColor ATTACK_ACTIVE_COLOR = new DebugColor(1.0f, 0.0f, 0.0f, 1.0f);
    private static final DebugColor ATTACK_INACTIVE_COLOR = new DebugColor(0.7f, 0.15f, 0.15f, 0.55f);
    private static final DebugColor WHITE = new DebugColor(1.0f, 1.0f, 1.0f, 1.0f);
    private static final String ANIMATION_EVENT_ATTACK_ACTIVE = "attack_active";
    private static final String PLAYER_IDLE_TEXTURE = "player_idle";
    private static final String PLAYER_RUN_TEXTURE = "player_run";
    private static final String PLAYER_JUMP_TEXTURE = "player_jump";
    private static final String PLAYER_FALL_TEXTURE = "player_fall";
    private static final String PLAYER_ATTACK_TEXTURE = "player_attack";
    private static final String PLAYER_HIT_TEXTURE = "player_hit";
    private static final String PLAYER_DEATH_TEXTURE = "player_death";
    private static final float PLAYER_SPRITE_OFFSET_X = -46.0f;
    private static final float PLAYER_SPRITE_OFFSET_Y = 0.0f;
    private static final String SLIME_TEXTURE_PREFIX = "slime_green_";
    private static final String IMPALER_TEXTURE_PREFIX = "impaler_";
    private static final float EDITOR_BUTTON_X = 12.0f;
    private static final float EDITOR_BUTTON_TOP_Y = 12.0f;
    private static final float EDITOR_BUTTON_WIDTH = 132.0f;
    private static final float EDITOR_BUTTON_HEIGHT = 24.0f;
    private static final float EDITOR_BUTTON_GAP = 6.0f;
    private static final String START_ROOM_ID = "forest_test_01";
    private static final String DEFAULT_PLAYER_SPAWN_ID = "entry_left";
    private static final String ABILITY_DASH = "dash";
    private static final String ABILITY_DOUBLE_JUMP = "double_jump";
    private static final String IMPALER_BOSS_ID = "impaler";
    private static final String SAMPLE_SAVE_PATH = ".umbra2d/sample-save.json";
    private static final float CHECKPOINT_TRIGGER_WIDTH = 40.0f;
    private static final float CHECKPOINT_TRIGGER_HEIGHT = 48.0f;
    private static final float DEATH_RESPAWN_DELAY_SECONDS = 1.0f;
    private static final float ROOM_TRANSITION_LOCKOUT_SECONDS = 0.35f;
    private static final float PLAYER_DASH_SPEED = 430.0f;
    private static final float PLAYER_DASH_DURATION_SECONDS = 0.16f;
    private static final float PLAYER_DASH_COOLDOWN_SECONDS = 0.35f;
    private static final float BOSS_ATTACK_REACH_PADDING = 6.0f;
    private static final float BOSS_PHASE_ONE_SPEED = 54.0f;
    private static final float BOSS_PHASE_TWO_SPEED = 76.0f;
    private static final float BOSS_HEALTH_BAR_WIDTH = 360.0f;
    private static final float BOSS_HEALTH_BAR_HEIGHT = 14.0f;

    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final BitmapFont editorFont = new BitmapFont();
    private final Matrix4 uiProjection = new Matrix4();
    private final LibGdxSpriteBatchRenderer spriteRenderer;
    private final LibGdxDebugShapeRenderer debugRenderer;
    private final DebugGeometryBuilder debugGeometryBuilder = new DebugGeometryBuilder();
    private final AnimationSetValidator animationSetValidator = new AnimationSetValidator();
    private final RoomTriggerResolver roomTriggerResolver = new RoomTriggerResolver();
    private final AbilityTriggerResolver abilityTriggerResolver = new AbilityTriggerResolver();
    private final SaveGameCodec saveGameCodec = new SaveGameCodec();
    private final Camera camera;
    private final EngineConfig config;
    private final RoomRegistry roomRegistry;
    private RoomDefinition room;
    private CollisionGrid grid;
    private final AnimationSetDefinition playerAnimationSet;
    private final AnimationSetDefinition slimeAnimationSet;
    private final AnimationSetDefinition goblinAnimationSet;
    private final AnimationSetDefinition flyingEyeAnimationSet;
    private final AnimationSetDefinition skeletonAnimationSet;
    private final AnimationSetDefinition mushroomAnimationSet;
    private final AnimationSetDefinition impalerAnimationSet;
    private final AnimationPlayer playerAnimator = new AnimationPlayer();
    private final KinematicBody player;
    private final PlayerController controller;
    private final KinematicImpulseConfig playerImpulseConfig;
    private final KinematicImpulseMover playerKnockbackMover = new KinematicImpulseMover();
    private final KinematicMover playerDashMover = new KinematicMover();
    private final List<EnemyActor> enemies = new ArrayList<>();
    private final CombatResolver combatResolver = new CombatResolver();
    private final AttackTimelinePlayer attackTimeline = new AttackTimelinePlayer();
    private final AttackTimelinePlayer bossAttackTimeline = new AttackTimelinePlayer();
    private final BossAttackSelector bossAttackSelector = new BossAttackSelector();
    private final List<BossAttackPattern> bossAttackPatterns = List.of(
            new BossAttackPattern("impaler_stab", "phase_1", 0.0f, 72.0f, 1.05f),
            new BossAttackPattern("impaler_sweep", "phase_2", 0.0f, 72.0f, 0.76f)
    );
    private final AttackTimelineDefinition slashTimeline = new AttackTimelineDefinition(
            new AttackDefinition("player_slash_01", 1, 160.0f, 40.0f, 0.045f, 0.18f, "slash"),
            0.06f,
            0.10f,
            0.18f
    );
    private final AttackTimelineDefinition bossAttackTimelineDefinition = new AttackTimelineDefinition(
            new AttackDefinition("impaler_spear", 1, 180.0f, 70.0f, 0.05f, 0.24f, "boss_spear"),
            0.26f,
            0.18f,
            0.36f
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
    private String currentRoomId = START_ROOM_ID;
    private final CheckpointState checkpointState = new CheckpointState(START_ROOM_ID, DEFAULT_PLAYER_SPAWN_ID);
    private final Set<String> visitedRoomIds = new LinkedHashSet<>();
    private final AbilityState abilityState = new AbilityState();
    private final Set<String> worldFlagIds = new LinkedHashSet<>();
    private BossActor boss;
    private BossArenaController bossArenaController;
    private BossArenaStatus bossArenaStatus;
    private final List<HitboxInstance> currentBossAttackHitboxes = new ArrayList<>();
    private float deathRespawnSeconds;
    private float roomTransitionLockoutSeconds;
    private float playerDashSeconds;
    private float playerDashCooldownSeconds;
    private int playerDashDirection = 1;

    TestRoomScene(SpriteBatch spriteBatch, ShapeRenderer shapes, Camera camera, EngineConfig config) {
        this.batch = spriteBatch;
        this.shapes = shapes;
        this.spriteRenderer = new LibGdxSpriteBatchRenderer(spriteBatch);
        this.debugRenderer = new LibGdxDebugShapeRenderer(shapes);
        this.camera = camera;
        this.config = config;
        this.playerAnimationSet = loadAnimationSet("/metadata/player_knight.anim.json",
                List.of("idle", "run", "jump", "fall", "attack", "hit", "death"));
        this.slimeAnimationSet = loadAnimationSet("/metadata/slime_green.anim.json",
                List.of("move", "idle", "attack", "take_hit", "death"));
        this.goblinAnimationSet = loadAnimationSet("/metadata/goblin.anim.json",
                List.of("move", "idle", "attack", "take_hit", "death"));
        this.flyingEyeAnimationSet = loadAnimationSet("/metadata/flying_eye.anim.json",
                List.of("move", "attack", "take_hit", "death"));
        this.skeletonAnimationSet = loadAnimationSet("/metadata/skeleton.anim.json",
                List.of("move", "attack", "take_hit", "death", "shield"));
        this.mushroomAnimationSet = loadAnimationSet("/metadata/mushroom.anim.json",
                List.of("move", "idle", "attack", "take_hit", "death"));
        this.impalerAnimationSet = loadAnimationSet("/metadata/impaler.anim.json",
                List.of("idle", "move", "attack", "death"));
        loadExternalSprites();
        this.roomRegistry = loadRoomRegistry();
        visitedRoomIds.add(START_ROOM_ID);
        loadCheckpointSave();
        this.room = roomRegistry.room(currentRoomId);
        this.grid = createGrid(room);
        RoomDefinition.SpawnPoint playerSpawn = findSpawn(DEFAULT_PLAYER_SPAWN_ID, 96.0f, 160.0f);
        this.player = new KinematicBody(playerSpawn.x(), playerSpawn.y(), 18.0f, 38.0f);
        PlayerControllerConfig createdPlayerConfig = PlayerControllerConfig.metroidvaniaDefaults();
        this.playerImpulseConfig = new KinematicImpulseConfig(
                createdPlayerConfig.gravityPixelsPerSecondSquared(),
                createdPlayerConfig.maxFallSpeedPixelsPerSecond()
        );
        this.controller = new PlayerController(createdPlayerConfig);
        createEnemies();
        createBoss();
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.K)
                || Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)
                || Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_RIGHT)) {
            startPlayerDash(input);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            respawnAtCheckpoint();
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
                playerDashSeconds = 0.0f;
                updatePlayerHitStun(deltaSeconds);
            } else if (playerDashSeconds > 0.0f) {
                updatePlayerDash(deltaSeconds);
            } else {
                updatePlayerDashCooldown(deltaSeconds);
                state = controller.update(input, player, grid, deltaSeconds, unlockedAirJumps());
            }
        }
        updateAbilityPickups();
        resolveAbilityGates();
        updateBossArena();
        updateEnemies(deltaSeconds);
        updateBoss(deltaSeconds);
        updateAnimations(deltaSeconds);
        updateCombat(deltaSeconds);
        updateCheckpoints();
        updateDoorTransitions();
        updateDeathRespawn(deltaSeconds);
        clampCameraToRoom();
    }

    @Override
    public void render() {
        drawRoom();
        drawEnemies();
        drawBoss();
        drawPlayer();
        drawCombatDebug();
        drawEditorOverlay();
        drawBossHud();
        drawHud();
    }

    private RoomDefinition loadRoom(String roomId) {
        try (InputStreamReader reader = new InputStreamReader(
                Objects.requireNonNull(TestRoomScene.class.getResourceAsStream("/rooms/" + roomId + ".json")),
                StandardCharsets.UTF_8
        )) {
            return new RoomLoader().load(reader);
        } catch (IOException exception) {
            throw new IllegalStateException("failed to close room resource", exception);
        }
    }

    private RoomRegistry loadRoomRegistry() {
        return new RoomRegistry(List.of(
                loadRoom("forest_test_01"),
                loadRoom("forest_test_02"),
                loadRoom("forest_boss_01")
        ));
    }

    private CollisionGrid createGrid(RoomDefinition room) {
        CollisionGrid createdGrid = new CollisionGrid(room.widthTiles(), room.heightTiles(), room.tileSize());
        for (RoomDefinition.TileCell tile : room.solidTiles()) {
            createdGrid.setSolid(tile.x(), tile.y(), true);
        }
        return createdGrid;
    }

    private void loadCheckpointSave() {
        FileHandle saveFile = Gdx.files.local(SAMPLE_SAVE_PATH);
        if (!saveFile.exists()) {
            return;
        }
        try {
            SaveGame saveGame = saveGameCodec.decode(saveFile.readString(StandardCharsets.UTF_8.name()));
            checkpointState.activate(saveGame.checkpointRoomId(), saveGame.checkpointSpawnId());
            visitedRoomIds.addAll(saveGame.visitedRoomIds());
            abilityState.unlockAll(saveGame.unlockedAbilityIds());
            worldFlagIds.addAll(saveGame.worldFlagIds());
            currentRoomId = saveGame.checkpointRoomId();
        } catch (RuntimeException exception) {
            Gdx.app.error("Umbra2D", "Ignoring invalid sample save: " + SAMPLE_SAVE_PATH, exception);
        }
    }

    private void persistCheckpointSave() {
        try {
            FileHandle saveFile = Gdx.files.local(SAMPLE_SAVE_PATH);
            saveFile.parent().mkdirs();
            SaveGame saveGame = new SaveGame(
                    SaveGame.CURRENT_VERSION,
                    checkpointState.roomId(),
                    checkpointState.spawnId(),
                    List.copyOf(visitedRoomIds),
                    abilityState.unlockedAbilityIds(),
                    List.copyOf(worldFlagIds)
            );
            saveFile.writeString(saveGameCodec.encode(saveGame), false, StandardCharsets.UTF_8.name());
        } catch (RuntimeException exception) {
            Gdx.app.error("Umbra2D", "Failed to write sample save: " + SAMPLE_SAVE_PATH, exception);
        }
    }

    private void updateDoorTransitions() {
        if (playerHealth.defeated()) {
            return;
        }
        if (roomTransitionLockoutSeconds > 0.0f) {
            return;
        }
        Aabb playerBounds = player.bounds();
        Optional<RoomDefinition.DoorDefinition> lockedDoor = findLockedBossDoor(playerBounds);
        if (lockedDoor.isPresent()) {
            RoomDefinition.DoorDefinition door = lockedDoor.get();
            Aabb doorBounds = new Aabb(door.x(), door.y(), door.width(), door.height());
            player.setPosition(abilityTriggerResolver.resolveHorizontalBlockX(playerBounds, doorBounds), player.y());
            player.setVelocityX(0.0f);
            playerDashSeconds = 0.0f;
            return;
        }
        Optional<RoomTransitionRequest> request = roomTriggerResolver.findDoorTransition(
                room,
                currentRoomId,
                playerBounds.x(),
                playerBounds.y(),
                playerBounds.width(),
                playerBounds.height()
        );
        if (request.isPresent()) {
            transitionToRoom(request.get().targetRoomId(), request.get().targetSpawnId());
        }
    }

    private Optional<RoomDefinition.DoorDefinition> findLockedBossDoor(Aabb playerBounds) {
        if (bossArenaStatus == null || !bossArenaStatus.locked()) {
            return Optional.empty();
        }
        for (RoomDefinition.DoorDefinition door : room.doors()) {
            if (!bossArenaStatus.lockedDoorIds().contains(door.id())) {
                continue;
            }
            Aabb doorBounds = new Aabb(door.x(), door.y(), door.width(), door.height());
            if (playerBounds.overlaps(doorBounds)) {
                return Optional.of(door);
            }
        }
        return Optional.empty();
    }

    private void transitionToRoom(String roomId, String spawnId) {
        currentRoomId = roomId;
        visitedRoomIds.add(roomId);
        room = roomRegistry.room(roomId);
        grid = createGrid(room);
        RoomDefinition.SpawnPoint spawn = findSpawn(spawnId, 96.0f, 160.0f);
        resetPlayerAt(spawn);
        clearTransientCombatState();
        createEnemies();
        createBoss();
        selectedEnemy = null;
        roomTransitionLockoutSeconds = ROOM_TRANSITION_LOCKOUT_SECONDS;
        clampCameraToRoom();
    }

    private void updateCheckpoints() {
        if (playerHealth.defeated()) {
            return;
        }
        Aabb playerBounds = player.bounds();
        Optional<String> checkpointId = roomTriggerResolver.findCheckpoint(
                room,
                playerBounds.x(),
                playerBounds.y(),
                playerBounds.width(),
                playerBounds.height(),
                CHECKPOINT_TRIGGER_WIDTH,
                CHECKPOINT_TRIGGER_HEIGHT
        );
        if (checkpointId.isPresent()) {
            String spawnId = checkpointId.get();
            if (!checkpointState.roomId().equals(currentRoomId) || !checkpointState.spawnId().equals(spawnId)) {
                checkpointState.activate(currentRoomId, spawnId);
                persistCheckpointSave();
            }
        }
    }

    private void updateAbilityPickups() {
        if (playerHealth.defeated()) {
            return;
        }
        Aabb playerBounds = player.bounds();
        Optional<String> abilityId = abilityTriggerResolver.findUnlockablePickup(
                abilityState,
                room.abilityPickups(),
                playerBounds
        );
        if (abilityId.isPresent() && abilityState.unlock(abilityId.get())) {
            persistCheckpointSave();
        }
    }

    private void resolveAbilityGates() {
        if (playerHealth.defeated()) {
            return;
        }
        Aabb playerBounds = player.bounds();
        Optional<AbilityGateBlock> block = abilityTriggerResolver.findBlockingGate(
                abilityState,
                room.abilityGates(),
                playerBounds
        );
        if (block.isPresent()) {
            float resolvedX = abilityTriggerResolver.resolveHorizontalBlockX(playerBounds, block.get().gateBounds());
            player.setPosition(resolvedX, player.y());
            player.setVelocityX(0.0f);
            playerDashSeconds = 0.0f;
        }
    }

    private void startPlayerDash(PlayerInput input) {
        if (!abilityState.has(ABILITY_DASH)
                || playerHealth.defeated()
                || playerHitStunTimer.stunned()
                || !attackTimeline.acceptingNewAttack()
                || playerDashCooldownSeconds > 0.0f
                || playerDashSeconds > 0.0f) {
            return;
        }
        if (input.left() && !input.right()) {
            playerDashDirection = -1;
        } else if (input.right() && !input.left()) {
            playerDashDirection = 1;
        } else {
            playerDashDirection = facingRight ? 1 : -1;
        }
        playerDashSeconds = PLAYER_DASH_DURATION_SECONDS;
        playerDashCooldownSeconds = PLAYER_DASH_COOLDOWN_SECONDS;
        player.setVelocityY(0.0f);
        state = PlayerState.RUN;
    }

    private void updatePlayerDash(float deltaSeconds) {
        updatePlayerDashCooldown(deltaSeconds);
        playerDashSeconds = Math.max(0.0f, playerDashSeconds - deltaSeconds);
        float velocityX = playerDashDirection * PLAYER_DASH_SPEED;
        player.setVelocityX(velocityX);
        player.setVelocityY(0.0f);
        MovementResult result = playerDashMover.move(player, grid, velocityX * deltaSeconds, 0.0f);
        if ((result.hitLeft() && velocityX < 0.0f) || (result.hitRight() && velocityX > 0.0f)) {
            playerDashSeconds = 0.0f;
            player.setVelocityX(0.0f);
        }
        if (playerDashSeconds <= 0.0f) {
            player.setVelocityX(0.0f);
        }
        state = PlayerState.RUN;
    }

    private void updatePlayerDashCooldown(float deltaSeconds) {
        playerDashCooldownSeconds = Math.max(0.0f, playerDashCooldownSeconds - deltaSeconds);
    }

    private int unlockedAirJumps() {
        return abilityState.has(ABILITY_DOUBLE_JUMP) ? 1 : 0;
    }

    private void updateDeathRespawn(float deltaSeconds) {
        if (!playerHealth.defeated()) {
            deathRespawnSeconds = 0.0f;
            roomTransitionLockoutSeconds = Math.max(0.0f, roomTransitionLockoutSeconds - deltaSeconds);
            return;
        }
        deathRespawnSeconds += deltaSeconds;
        if (deathRespawnSeconds >= DEATH_RESPAWN_DELAY_SECONDS) {
            respawnAtCheckpoint();
        }
    }

    private void respawnAtCheckpoint() {
        RoomTransitionRequest request = checkpointState.respawnRequest();
        transitionToRoom(request.targetRoomId(), request.targetSpawnId());
        playerHealth.reset();
        deathRespawnSeconds = 0.0f;
        playerAnimator.restart(playerAnimationSet.clip("idle"));
    }

    private void resetPlayerAt(RoomDefinition.SpawnPoint spawn) {
        player.setPosition(spawn.x(), spawn.y());
        player.setVelocityX(0.0f);
        player.setVelocityY(0.0f);
        state = PlayerState.IDLE;
        facingRight = true;
        attackFacingRight = true;
        playerDashSeconds = 0.0f;
        playerDashCooldownSeconds = 0.0f;
    }

    private void clearTransientCombatState() {
        hitPauseTimer.reset();
        playerHitStunTimer.reset();
        currentAttackHitbox = null;
        currentBossAttackHitboxes.clear();
        attackTimeline.reset();
        bossAttackTimeline.reset();
        bossAttackSelector.reset();
        playerDashSeconds = 0.0f;
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
        enemies.clear();
        for (RoomDefinition.SpawnPoint spawn : room.spawns()) {
            if (!spawn.type().equals("enemy_spawn")) {
                continue;
            }
            EnemyKind kind = enemyKindForSpawn(spawn.id());
            if (kind != null) {
                enemies.add(createEnemy(kind, spawn.id(), spawn.x(), spawn.y()));
            }
        }
    }

    private void createBoss() {
        boss = null;
        bossArenaController = null;
        bossArenaStatus = null;
        currentBossAttackHitboxes.clear();
        bossAttackTimeline.reset();
        bossAttackSelector.reset();
        if (room.bossArenas().isEmpty()) {
            return;
        }

        RoomDefinition.BossArenaDefinition roomArena = room.bossArenas().get(0);
        bossArenaController = new BossArenaController(toBossArenaDefinition(roomArena), List.of(
                new BossPhaseDefinition("phase_1", 1.0f),
                new BossPhaseDefinition("phase_2", 0.5f)
        ));
        if (!worldFlagIds.contains(roomArena.defeatFlagId())) {
            for (RoomDefinition.SpawnPoint spawn : room.spawns()) {
                if (spawn.type().equals("boss_spawn") && spawn.id().startsWith(IMPALER_BOSS_ID)) {
                    boss = new BossActor(spawn.id(), spawn.x(), spawn.y(), impalerAnimationSet);
                    break;
                }
            }
        }
        updateBossArena();
    }

    private BossArenaDefinition toBossArenaDefinition(RoomDefinition.BossArenaDefinition roomArena) {
        RoomDefinition.RectDefinition arena = roomArena.arena();
        RoomDefinition.RectDefinition activation = roomArena.activation();
        return new BossArenaDefinition(
                roomArena.id(),
                roomArena.bossId(),
                roomArena.defeatFlagId(),
                new Aabb(arena.x(), arena.y(), arena.width(), arena.height()),
                new Aabb(activation.x(), activation.y(), activation.width(), activation.height()),
                roomArena.lockedDoorIds()
        );
    }

    private EnemyKind enemyKindForSpawn(String spawnId) {
        for (EnemyKind kind : EnemyKind.values()) {
            if (spawnId.startsWith(kind.spawnPrefix)) {
                return kind;
            }
        }
        return null;
    }

    private EnemyActor createEnemy(EnemyKind kind, String spawnId, float x, float y) {
        RoomDefinition.SpawnPoint spawn = new RoomDefinition.SpawnPoint(spawnId, "enemy_spawn", x, y);
        return switch (kind) {
            case SLIME -> createGroundEnemy(
                kind,
                EnemyArchetype.WALKER,
                spawn,
                slimeAnimationSet,
                28.0f,
                18.0f,
                new HitboxDefinition(26.0f, 18.0f, 2.0f, 0.0f),
                96.0f,
                96.0f,
                0.0f,
                -35.0f,
                new DebugColor(0.30f, 0.85f, 0.32f, 1.0f),
                PatrolControllerConfig.slimeDefaults(),
                enemyBrainConfig(180.0f, 64.0f, 34.0f, 0.20f, 0.50f, 44.0f, 0.35f, 0.18f),
                76.0f,
                118.0f
            );
            case GOBLIN -> createGroundEnemy(
                kind,
                EnemyArchetype.CHARGER,
                spawn,
                goblinAnimationSet,
                26.0f,
                42.0f,
                new HitboxDefinition(34.0f, 28.0f, 0.0f, 7.0f),
                78.0f,
                78.0f,
                0.0f,
                -18.0f,
                new DebugColor(0.35f, 0.75f, 0.28f, 1.0f),
                new PatrolControllerConfig(70.0f, 1200.0f, 420.0f),
                EnemyBrainConfig.standardMelee(),
                104.0f,
                140.0f
            );
            case FLYING_EYE -> createFlyingEnemy(
                kind,
                EnemyArchetype.FLYER,
                spawn,
                flyingEyeAnimationSet,
                30.0f,
                26.0f,
                new HitboxDefinition(18.0f, 18.0f, 2.0f, 2.0f),
                72.0f,
                72.0f,
                0.0f,
                -22.0f,
                new DebugColor(0.90f, 0.25f, 0.55f, 1.0f),
                64.0f,
                EnemyBrainConfig.flyingMelee(),
                96.0f,
                142.0f
            );
            case SKELETON -> createGroundEnemy(
                kind,
                EnemyArchetype.WALKER,
                spawn,
                skeletonAnimationSet,
                24.0f,
                46.0f,
                new HitboxDefinition(34.0f, 30.0f, 0.0f, 8.0f),
                82.0f,
                82.0f,
                0.0f,
                -22.0f,
                new DebugColor(0.82f, 0.82f, 0.70f, 1.0f),
                new PatrolControllerConfig(48.0f, 1200.0f, 420.0f),
                EnemyBrainConfig.standardMelee(),
                78.0f,
                116.0f
            );
            case MUSHROOM -> createGroundEnemy(
                kind,
                EnemyArchetype.WALKER,
                spawn,
                mushroomAnimationSet,
                30.0f,
                34.0f,
                new HitboxDefinition(18.0f, 18.0f, 2.0f, 4.0f),
                74.0f,
                74.0f,
                0.0f,
                -18.0f,
                new DebugColor(0.88f, 0.33f, 0.34f, 1.0f),
                new PatrolControllerConfig(42.0f, 1200.0f, 420.0f),
                enemyBrainConfig(240.0f, 96.0f, 42.0f, 0.28f, 0.62f, 56.0f, 0.45f, 0.22f),
                70.0f,
                108.0f
            );
        };
    }

    private EnemyBrainConfig enemyBrainConfig(
            float visionRangeX,
            float visionRangeY,
            float weaponRange,
            float attackWindupSeconds,
            float attackDurationSeconds,
            float evadeThreatRange,
            float evadeChance,
            float evadeDurationSeconds
    ) {
        return new EnemyBrainConfig(
                visionRangeX,
                visionRangeY,
                weaponRange,
                weaponRange,
                attackWindupSeconds,
                attackDurationSeconds,
                evadeThreatRange,
                evadeChance,
                evadeDurationSeconds
        );
    }

    private EnemyActor createGroundEnemy(
            EnemyKind kind,
            EnemyArchetype archetype,
            RoomDefinition.SpawnPoint spawn,
            AnimationSetDefinition animationSet,
            float bodyWidth,
            float bodyHeight,
            HitboxDefinition attackHitboxDefinition,
            float drawWidth,
            float drawHeight,
            float drawOffsetX,
            float drawOffsetY,
            DebugColor fallbackColor,
            PatrolControllerConfig patrolConfig,
            EnemyBrainConfig brainConfig,
            float chaseSpeed,
            float evadeSpeed
    ) {
        return new EnemyActor(
                nextEnemyEntityId++,
                kind,
                archetype,
                spawn.id(),
                EnemyMovement.GROUND_PATROL,
                spawn.x(),
                spawn.y(),
                new KinematicBody(spawn.x(), spawn.y(), bodyWidth, bodyHeight),
                animationSet,
                attackHitboxDefinition,
                new PatrolController(patrolConfig, -1),
                new KinematicImpulseConfig(patrolConfig.gravityPixelsPerSecondSquared(), patrolConfig.maxFallSpeedPixelsPerSecond()),
                0.0f,
                brainConfig,
                chaseSpeed,
                evadeSpeed,
                drawWidth,
                drawHeight,
                drawOffsetX,
                drawOffsetY,
                fallbackColor
        );
    }

    private EnemyActor createFlyingEnemy(
            EnemyKind kind,
            EnemyArchetype archetype,
            RoomDefinition.SpawnPoint spawn,
            AnimationSetDefinition animationSet,
            float bodyWidth,
            float bodyHeight,
            HitboxDefinition attackHitboxDefinition,
            float drawWidth,
            float drawHeight,
            float drawOffsetX,
            float drawOffsetY,
            DebugColor fallbackColor,
            float speed,
            EnemyBrainConfig brainConfig,
            float chaseSpeed,
            float evadeSpeed
    ) {
        return new EnemyActor(
                nextEnemyEntityId++,
                kind,
                archetype,
                spawn.id(),
                EnemyMovement.FLYING_PATROL,
                spawn.x(),
                spawn.y(),
                new KinematicBody(spawn.x(), spawn.y(), bodyWidth, bodyHeight),
                animationSet,
                attackHitboxDefinition,
                null,
                new KinematicImpulseConfig(800.0f, 360.0f),
                speed,
                brainConfig,
                chaseSpeed,
                evadeSpeed,
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
        if (boss != null) {
            boss.health.update(deltaSeconds);
        }
        attackTimeline.update(deltaSeconds);
        bossAttackTimeline.update(deltaSeconds);

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
            if (boss != null && !boss.health.defeated() && bossArenaStatus != null && bossArenaStatus.active()) {
                enemyHurtboxes.add(new HurtboxInstance(BOSS_ENTITY_ID, CombatTeam.ENEMY, boss.body.bounds(), true));
            }
            List<DamageEvent> damageEvents = combatResolver.resolve(List.of(currentAttackHitbox), enemyHurtboxes);
            for (DamageEvent event : damageEvents) {
                if (event.targetEntityId() == BOSS_ENTITY_ID && boss != null) {
                    DamageApplication application = boss.health.apply(event);
                    if (application.applied()) {
                        hitPauseTimer.trigger(event.hitPauseSeconds());
                        if (application.defeated()) {
                            handleBossDefeated();
                        }
                    }
                } else {
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
            }

            if (attackTimeline.phase() == AttackPhase.FINISHED) {
                currentAttackHitbox = null;
            }
        }

        if (!playerHealth.defeated()) {
            List<HitboxInstance> enemyAttackHitboxes = new ArrayList<>();
            for (EnemyActor enemy : enemies) {
                if (!enemy.health.defeated() && enemyAttackActive(enemy)) {
                    enemyAttackHitboxes.add(enemy.createAttackHitbox(enemyContactAttack));
                }
            }
            currentBossAttackHitboxes.clear();
            if (boss != null && !boss.health.defeated() && bossAttackActive()) {
                currentBossAttackHitboxes.addAll(boss.createAttack2Hitboxes(
                        bossAttackTimelineDefinition.attack(),
                        boss.animator.frameIndex()
                ));
                enemyAttackHitboxes.addAll(currentBossAttackHitboxes);
            }
            List<DamageEvent> damageEvents = combatResolver.resolve(
                    enemyAttackHitboxes,
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
        if (bossAttackTimeline.phase() == AttackPhase.FINISHED) {
            currentBossAttackHitboxes.clear();
        }
    }

    private void handleBossDefeated() {
        if (bossArenaController != null && worldFlagIds.add(bossArenaController.definition().defeatFlagId())) {
            persistCheckpointSave();
        }
        currentBossAttackHitboxes.clear();
        bossAttackTimeline.reset();
        updateBossArena();
    }

    private boolean enemyAttackActive(EnemyActor enemy) {
        AnimationClipDefinition clip = enemy.animator.clip();
        return enemy.brain.state() == EnemyAiState.ATTACK
                && clip != null
                && clip.id().equals("attack")
                && clip.hasEvent(enemy.animator.frameIndex(), ANIMATION_EVENT_ATTACK_ACTIVE);
    }

    private boolean bossAttackActive() {
        if (boss == null) {
            return false;
        }
        AnimationClipDefinition clip = boss.animator.clip();
        return bossArenaStatus != null
                && bossArenaStatus.active()
                && clip != null
                && clip.id().equals("attack")
                && boss.animator.frameIndex() >= 1
                && boss.animator.frameIndex() <= 6;
    }

    private EnemyActor findEnemy(int entityId) {
        for (EnemyActor enemy : enemies) {
            if (enemy.entityId == entityId) {
                return enemy;
            }
        }
        return null;
    }

    private AnimationSetDefinition loadAnimationSet(String resourcePath, List<String> requiredClipIds) {
        try (InputStreamReader reader = new InputStreamReader(
                Objects.requireNonNull(TestRoomScene.class.getResourceAsStream(resourcePath)),
                StandardCharsets.UTF_8
        )) {
            AnimationSetDefinition animationSet = new AnimationMetadataLoader().load(reader);
            animationSetValidator.requireClips(animationSet, requiredClipIds);
            return animationSet;
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

    private void updateBossArena() {
        if (bossArenaController == null) {
            bossArenaStatus = null;
            return;
        }
        boolean defeatFlagSet = worldFlagIds.contains(bossArenaController.definition().defeatFlagId());
        int currentHealth = boss == null || defeatFlagSet ? 0 : boss.health.currentHealth();
        int maxHealth = boss == null ? BossActor.MAX_HEALTH : boss.health.maxHealth();
        bossArenaStatus = bossArenaController.update(new BossArenaInput(
                player.bounds(),
                currentHealth,
                maxHealth,
                defeatFlagSet
        ));
    }

    private void updateBoss(float deltaSeconds) {
        if (boss == null || boss.health.defeated() || bossArenaStatus == null || !bossArenaStatus.active()) {
            return;
        }
        float bossCenterX = boss.body.x() + boss.body.width() * 0.5f;
        float playerCenterX = player.x() + player.width() * 0.5f;
        float dx = playerCenterX - bossCenterX;
        float edgeDistance = boss.edgeDistanceTo(player.bounds());
        boss.facingDirection = dx >= 0.0f ? 1 : -1;

        if (!bossAttackTimeline.acceptingNewAttack()) {
            return;
        }
        if (boss.attack2CanReach(player.bounds())) {
            Optional<BossAttackPattern> selectedAttack = bossAttackSelector.update(
                    bossArenaStatus.phase().id(),
                    edgeDistance,
                    bossAttackPatterns,
                    deltaSeconds
            );
            if (selectedAttack.isPresent()) {
                bossAttackTimeline.start(bossAttackTimelineDefinition);
                currentBossAttackHitboxes.clear();
                boss.animator.restart(boss.animationSet.clip("attack"));
                boss.body.setVelocityX(0.0f);
                return;
            }
            boss.body.setVelocityX(0.0f);
            return;
        }
        float speed = bossArenaStatus.phase().id().equals("phase_2") ? BOSS_PHASE_TWO_SPEED : BOSS_PHASE_ONE_SPEED;
        float nextX = boss.body.x() + boss.facingDirection * speed * deltaSeconds;
        boss.body.setPosition(clampBossX(boss, nextX), boss.body.y());
        boss.body.setVelocityX(boss.facingDirection * speed);
    }

    private float clampBossX(BossActor actor, float x) {
        if (bossArenaController == null) {
            return x;
        }
        Aabb arena = bossArenaController.definition().arenaBounds();
        return Math.max(arena.x(), Math.min(x, arena.right() - actor.body.width()));
    }

    private void updateEnemies(float deltaSeconds) {
        boolean playerAttackThreat = currentAttackHitbox != null && !attackTimeline.acceptingNewAttack();
        for (EnemyActor enemy : enemies) {
            EnemyBrainDecision decision = enemy.brain.update(new EnemyBrainInput(
                    enemy.body.x() + enemy.body.width() * 0.5f,
                    enemy.body.y() + enemy.body.height() * 0.5f,
                    player.x() + player.width() * 0.5f,
                    player.y() + player.height() * 0.5f,
                    playerAttackThreat,
                    enemy.hitStunTimer.stunned(),
                    enemy.health.defeated(),
                    deltaSeconds
            ));

            if (decision.desiredDirection() != 0) {
                enemy.facingDirection = decision.desiredDirection();
            }
            if (decision.state() == EnemyAiState.DEAD) {
                continue;
            }
            if (decision.state() == EnemyAiState.HIT_STUN) {
                enemy.knockbackMover.update(enemy.body, grid, enemy.impulseConfig, deltaSeconds);
                enemy.hitStunTimer.update(deltaSeconds);
                if (!enemy.hitStunTimer.stunned()) {
                    enemy.body.setVelocityX(0.0f);
                }
                continue;
            }

            if (enemy.movement == EnemyMovement.GROUND_PATROL) {
                updateGroundEnemy(enemy, decision, deltaSeconds);
            } else {
                updateFlyingEnemy(enemy, decision, deltaSeconds);
            }
        }
    }

    private void updateGroundEnemy(EnemyActor enemy, EnemyBrainDecision decision, float deltaSeconds) {
        if (decision.state() == EnemyAiState.PATROL) {
            enemy.patrolController.update(enemy.body, grid, deltaSeconds);
            enemy.facingDirection = enemy.patrolController.facingRight() ? 1 : -1;
            return;
        }

        float speed = switch (decision.state()) {
            case CHASE -> enemy.effectiveChaseSpeed();
            case EVADE -> enemy.evadeSpeed;
            default -> 0.0f;
        };
        float velocityX = decision.desiredDirection() * speed;
        float velocityY = Math.max(
                enemy.body.velocityY() - enemy.impulseConfig.gravityPixelsPerSecondSquared() * deltaSeconds,
                -enemy.impulseConfig.maxFallSpeedPixelsPerSecond()
        );
        enemy.body.setVelocityX(velocityX);
        enemy.body.setVelocityY(velocityY);
        MovementResult result = enemy.mover.move(enemy.body, grid, velocityX * deltaSeconds, velocityY * deltaSeconds);
        if ((result.hitLeft() && velocityX < 0.0f) || (result.hitRight() && velocityX > 0.0f)) {
            enemy.body.setVelocityX(0.0f);
        }
        if ((result.hitGround() && velocityY < 0.0f) || (result.hitCeiling() && velocityY > 0.0f)) {
            enemy.body.setVelocityY(0.0f);
        }
    }

    private void updateFlyingEnemy(EnemyActor enemy, EnemyBrainDecision decision, float deltaSeconds) {
        if (decision.state() == EnemyAiState.CHASE || decision.state() == EnemyAiState.EVADE) {
            float speed = decision.state() == EnemyAiState.CHASE ? enemy.effectiveChaseSpeed() : enemy.evadeSpeed;
            float nextX = enemy.body.x() + decision.desiredDirection() * speed * deltaSeconds;
            float targetCenterY = decision.state() == EnemyAiState.CHASE
                    ? player.y() + player.height() * 0.5f
                    : enemy.spawnY;
            float currentCenterY = enemy.body.y() + enemy.body.height() * 0.5f;
            float dy = Math.max(-speed, Math.min(speed, targetCenterY - currentCenterY)) * deltaSeconds;
            enemy.body.setPosition(clampEnemyX(enemy, nextX), clampEnemyY(enemy, enemy.body.y() + dy));
            return;
        }
        if (decision.state() == EnemyAiState.CAUTIOUS || decision.state() == EnemyAiState.ATTACK) {
            return;
        }

        enemy.flightAgeSeconds += deltaSeconds;
        float patrolHalfWidth = 96.0f;
        float nextX = enemy.body.x() + enemy.flightDirection * enemy.flightSpeed * deltaSeconds;
        if (nextX < enemy.spawnX - patrolHalfWidth || nextX > enemy.spawnX + patrolHalfWidth) {
            enemy.flightDirection *= -1;
            nextX = enemy.body.x() + enemy.flightDirection * enemy.flightSpeed * deltaSeconds;
        }
        float hoverTargetY = enemy.spawnY + (float) Math.sin(enemy.flightAgeSeconds * 3.0f) * 8.0f;
        float nextY = approach(enemy.body.y(), hoverTargetY, enemy.flightSpeed * deltaSeconds);
        enemy.body.setPosition(nextX, clampEnemyY(enemy, nextY));
    }

    private float approach(float current, float target, float maxDelta) {
        if (current < target) {
            return Math.min(current + maxDelta, target);
        }
        return Math.max(current - maxDelta, target);
    }

    private float clampEnemyX(EnemyActor enemy, float x) {
        float roomWidth = grid.widthTiles() * grid.tileSize();
        return Math.max(grid.tileSize(), Math.min(x, roomWidth - grid.tileSize() - enemy.body.width()));
    }

    private float clampEnemyY(EnemyActor enemy, float y) {
        float roomHeight = grid.heightTiles() * grid.tileSize();
        return Math.max(grid.tileSize(), Math.min(y, roomHeight - grid.tileSize() - enemy.body.height()));
    }

    private void updateAnimations(float deltaSeconds) {
        playerAnimator.play(resolvePlayerClip());
        playerAnimator.update(deltaSeconds);

        for (EnemyActor enemy : enemies) {
            playEnemyClip(enemy, resolveEnemyClip(enemy));
            enemy.animator.update(deltaSeconds);
        }
        if (boss != null) {
            playBossClip(resolveBossClip());
            boss.animator.update(deltaSeconds);
        }
    }

    private void playEnemyClip(EnemyActor enemy, AnimationClipDefinition clip) {
        AnimationClipDefinition currentClip = enemy.animator.clip();
        if (currentClip == null || !currentClip.id().equals(clip.id())) {
            enemy.animator.restart(clip);
            return;
        }
        enemy.animator.play(clip);
    }

    private void playBossClip(AnimationClipDefinition clip) {
        AnimationClipDefinition currentClip = boss.animator.clip();
        if (currentClip == null || !currentClip.id().equals(clip.id())) {
            boss.animator.restart(clip);
            return;
        }
        boss.animator.play(clip);
    }

    private AnimationClipDefinition resolveEnemyClip(EnemyActor enemy) {
        if (enemy.health.defeated()) {
            return clipOrMove(enemy.animationSet, "death");
        }
        if (enemy.hitStunTimer.stunned()) {
            return clipOrMove(enemy.animationSet, "take_hit");
        }
        if (enemy.brain.state() == EnemyAiState.ATTACK) {
            return clipOrMove(enemy.animationSet, "attack");
        }
        if (enemy.kind == EnemyKind.SLIME
                && (enemy.brain.state() == EnemyAiState.CHASE || enemy.brain.state() == EnemyAiState.EVADE)) {
            return clipOrMove(enemy.animationSet, "slide_loop");
        }
        if (enemy.kind == EnemyKind.SKELETON && enemy.usesShieldCaution()
                && (enemy.brain.state() == EnemyAiState.CHASE || enemy.brain.state() == EnemyAiState.CAUTIOUS)) {
            return clipOrMove(enemy.animationSet, "shield");
        }
        if (enemy.brain.state() == EnemyAiState.CAUTIOUS) {
            return clipOrMove(enemy.animationSet, "idle");
        }
        return enemy.animationSet.clip("move");
    }

    private AnimationClipDefinition resolveBossClip() {
        if (boss.health.defeated()) {
            return boss.animationSet.clip("death");
        }
        if (!bossAttackTimeline.acceptingNewAttack()) {
            return boss.animationSet.clip("attack");
        }
        if (bossArenaStatus != null && bossArenaStatus.active() && Math.abs(boss.body.velocityX()) > 0.01f) {
            return boss.animationSet.clip("move");
        }
        return boss.animationSet.clip("idle");
    }

    private AnimationClipDefinition clipOrMove(AnimationSetDefinition animationSet, String clipId) {
        for (AnimationClipDefinition clip : animationSet.clips()) {
            if (clip.id().equals(clipId)) {
                return clip;
            }
        }
        return animationSet.clip("move");
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
        float minX = halfWidth;
        float maxX = roomWidth - halfWidth;
        float minY = halfHeight;
        float maxY = roomHeight - halfHeight;
        if (bossArenaStatus != null && bossArenaStatus.active() && bossArenaController != null) {
            Aabb arena = bossArenaController.definition().arenaBounds();
            minX = Math.max(minX, arena.x() + halfWidth);
            maxX = Math.min(maxX, arena.right() - halfWidth);
            minY = Math.max(minY, arena.y() + halfHeight);
            maxY = Math.min(maxY, arena.top() - halfHeight);
        }
        float targetX = minX > maxX ? (minX + maxX) * 0.5f : Math.max(minX, Math.min(player.x(), maxX));
        float targetY = minY > maxY ? (minY + maxY) * 0.5f : Math.max(minY, Math.min(player.y(), maxY));
        camera.position.set(targetX, targetY, 0.0f);
    }

    private void drawRoom() {
        DebugDrawList drawList = new DebugDrawList();
        debugGeometryBuilder.addSolidTiles(drawList, grid, SOLID_TILE_COLOR);
        debugGeometryBuilder.addTileGrid(drawList, grid, GRID_COLOR);
        for (RoomDefinition.CameraZoneDefinition zone : room.cameraZones()) {
            debugGeometryBuilder.addAabb(drawList, new Aabb(zone.x(), zone.y(), zone.width(), zone.height()), CAMERA_ZONE_COLOR);
        }
        for (RoomDefinition.DoorDefinition door : room.doors()) {
            DebugColor color = bossArenaStatus != null
                    && bossArenaStatus.locked()
                    && bossArenaStatus.lockedDoorIds().contains(door.id())
                    ? BOSS_LOCKED_DOOR_COLOR
                    : DOOR_TRIGGER_COLOR;
            debugGeometryBuilder.addAabb(drawList, new Aabb(door.x(), door.y(), door.width(), door.height()), color);
        }
        for (RoomDefinition.BossArenaDefinition arena : room.bossArenas()) {
            RoomDefinition.RectDefinition bounds = arena.arena();
            debugGeometryBuilder.addAabb(drawList, new Aabb(bounds.x(), bounds.y(), bounds.width(), bounds.height()), BOSS_ARENA_COLOR);
        }
        for (RoomDefinition.SpawnPoint spawn : room.spawns()) {
            if (spawn.type().equals("checkpoint")) {
                debugGeometryBuilder.addAabb(drawList, new Aabb(
                        spawn.x() - CHECKPOINT_TRIGGER_WIDTH * 0.5f,
                        spawn.y(),
                        CHECKPOINT_TRIGGER_WIDTH,
                        CHECKPOINT_TRIGGER_HEIGHT
                ), CHECKPOINT_COLOR);
            }
        }
        for (RoomDefinition.AbilityPickupDefinition pickup : room.abilityPickups()) {
            if (!abilityState.has(pickup.abilityId())) {
                debugGeometryBuilder.addAabb(drawList, new Aabb(
                        pickup.x(),
                        pickup.y(),
                        pickup.width(),
                        pickup.height()
                ), ABILITY_PICKUP_COLOR);
            }
        }
        for (RoomDefinition.AbilityGateDefinition gate : room.abilityGates()) {
            DebugColor color = abilityState.has(gate.requiredAbilityId())
                    ? ABILITY_GATE_UNLOCKED_COLOR
                    : ABILITY_GATE_LOCKED_COLOR;
            debugGeometryBuilder.addAabb(drawList, new Aabb(
                    gate.x(),
                    gate.y(),
                    gate.width(),
                    gate.height()
            ), color);
        }
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
                    player.x() + PLAYER_SPRITE_OFFSET_X,
                    player.y() + PLAYER_SPRITE_OFFSET_Y,
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

    private void drawBoss() {
        if (boss == null) {
            return;
        }
        if (drawBossSprite()) {
            return;
        }
        DebugDrawList drawList = new DebugDrawList();
        drawList.addRect(new DebugRect(
                boss.body.x(),
                boss.body.y(),
                boss.body.width(),
                boss.body.height(),
                boss.health.defeated() ? new DebugColor(0.18f, 0.12f, 0.16f, 1.0f) : boss.fallbackColor,
                DebugShapeStyle.FILLED
        ));
        debugRenderer.render(drawList);
    }

    private boolean drawBossSprite() {
        AnimationClipDefinition clip = boss.animator.clip();
        int frameIndex = boss.animator.frameIndex();
        String textureId = clip == null ? null : clip.textureIdForFrame(frameIndex);
        if (textureId == null || !spriteRenderer.hasTexture(textureId)) {
            return false;
        }

        SpriteDrawList sprites = new SpriteDrawList();
        sprites.add(new SpriteDrawCommand(
                textureId,
                clip.sourceXForFrame(frameIndex),
                clip.sourceYForFrame(frameIndex),
                clip.frameWidth(),
                clip.frameHeight(),
                boss.drawX(),
                boss.body.y() + boss.drawOffsetY,
                boss.drawWidth,
                boss.drawHeight,
                boss.facingRight(),
                false,
                WHITE
        ));
        spriteRenderer.render(sprites);
        return true;
    }

    private boolean drawEnemySprite(EnemyActor enemy) {
        AnimationClipDefinition clip = enemy.animator.clip();
        int frameIndex = enemy.animator.frameIndex();
        String textureId = clip == null ? null : clip.textureIdForFrame(frameIndex);
        if (textureId == null || !spriteRenderer.hasTexture(textureId)) {
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
            if (!enemy.health.defeated() && enemyAttackActive(enemy)) {
                debugGeometryBuilder.addAabb(drawList, enemy.attackBounds(), ATTACK_ACTIVE_COLOR);
            }
        }
        if (boss != null) {
            debugGeometryBuilder.addAabb(drawList, boss.body.bounds(), BOSS_HURTBOX_COLOR);
            if (!boss.health.defeated() && bossAttackActive()) {
                for (Aabb attackBounds : boss.attack2Bounds(boss.animator.frameIndex())) {
                    debugGeometryBuilder.addAabb(drawList, attackBounds, ATTACK_ACTIVE_COLOR);
                }
            }
        }
        if (selectedEnemy != null && enemies.contains(selectedEnemy)) {
            drawSelectedEnemyAiDebug(drawList, selectedEnemy);
            debugGeometryBuilder.addAabb(drawList, selectedEnemy.body.bounds(), SELECTED_ENEMY_COLOR);
        }

        if (currentAttackHitbox != null) {
            DebugColor hitboxColor = currentAttackHitbox.active() ? ATTACK_ACTIVE_COLOR : ATTACK_INACTIVE_COLOR;
            Aabb hitbox = currentAttackHitbox.bounds();
            debugGeometryBuilder.addAabb(drawList, hitbox, hitboxColor);
        }
        for (HitboxInstance bossHitbox : currentBossAttackHitboxes) {
            debugGeometryBuilder.addAabb(
                    drawList,
                    bossHitbox.bounds(),
                    bossHitbox.active() ? ATTACK_ACTIVE_COLOR : ATTACK_INACTIVE_COLOR
            );
        }
        debugRenderer.render(drawList);
    }

    private void drawSelectedEnemyAiDebug(DebugDrawList drawList, EnemyActor enemy) {
        EnemyBrainConfig brainConfig = enemy.brain.config();
        float centerX = enemy.body.x() + enemy.body.width() * 0.5f;
        float centerY = enemy.body.y() + enemy.body.height() * 0.5f;
        debugGeometryBuilder.addAabb(
                drawList,
                new Aabb(
                        centerX - brainConfig.visionRangeX(),
                        centerY - brainConfig.visionRangeY(),
                        brainConfig.visionRangeX() * 2.0f,
                        brainConfig.visionRangeY() * 2.0f
                ),
                ENEMY_VISION_COLOR
        );
        debugGeometryBuilder.addAabb(
                drawList,
                new Aabb(
                        centerX - brainConfig.attackRange(),
                        enemy.body.y(),
                        brainConfig.attackRange() * 2.0f,
                        enemy.body.height()
                ),
                ENEMY_ATTACK_RANGE_COLOR
        );
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
            editorFont.draw(batch, "Selected: " + selectedEnemy.spawnId
                            + " #" + selectedEnemy.entityId
                            + " " + selectedEnemy.archetype
                            + " " + selectedEnemy.brain.state(),
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

    private void drawBossHud() {
        if (boss == null || bossArenaStatus == null || bossArenaStatus.state() == BossFightState.DORMANT) {
            return;
        }
        uiProjection.setToOrtho2D(0.0f, 0.0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapes.setProjectionMatrix(uiProjection);
        batch.setProjectionMatrix(uiProjection);

        float x = (Gdx.graphics.getWidth() - BOSS_HEALTH_BAR_WIDTH) * 0.5f;
        float y = Gdx.graphics.getHeight() - 40.0f;
        float ratio = boss.health.currentHealth() / (float) boss.health.maxHealth();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(new Color(0.06f, 0.04f, 0.05f, 0.92f));
        shapes.rect(x - 4.0f, y - 4.0f, BOSS_HEALTH_BAR_WIDTH + 8.0f, BOSS_HEALTH_BAR_HEIGHT + 8.0f);
        shapes.setColor(new Color(0.20f, 0.05f, 0.07f, 1.0f));
        shapes.rect(x, y, BOSS_HEALTH_BAR_WIDTH, BOSS_HEALTH_BAR_HEIGHT);
        shapes.setColor(new Color(0.82f, 0.14f, 0.10f, 1.0f));
        shapes.rect(x, y, BOSS_HEALTH_BAR_WIDTH * ratio, BOSS_HEALTH_BAR_HEIGHT);
        shapes.end();

        batch.begin();
        editorFont.setColor(Color.WHITE);
        editorFont.draw(batch,
                "Impaler " + boss.health.currentHealth() + "/" + boss.health.maxHealth()
                        + " " + bossArenaStatus.phase().id()
                        + (bossArenaStatus.locked() ? " arena locked" : " defeated"),
                x,
                y + 30.0f);
        batch.end();
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

        registerSlimeFrameSequence(assetRoot, "move", "walk", "walk", 6);
        registerSlimeFrameSequence(assetRoot, "idle", "idle", "idle", 8);
        registerSlimeFrameSequence(assetRoot, "attack", "attack", "attack", 12);
        registerSlimeFrameSequence(assetRoot, "take_hit", "hurt1", "hurt1", 5);
        registerSlimeFrameSequence(assetRoot, "death", "death", "death", 10);
        registerSlimeFrameSequence(assetRoot, "slide_loop", "slide_loop", "slide_loop", 6);
        registerFantasyMonsterSheet(assetRoot, "goblin_move", "Goblin", "Run-sheet.png");
        registerFantasyMonsterSheet(assetRoot, "goblin_idle", "Goblin", "Idle-sheet.png");
        registerFantasyMonsterSheet(assetRoot, "goblin_attack", "Goblin", "Attack-sheet.png");
        registerFantasyMonsterSheet(assetRoot, "goblin_take_hit", "Goblin", "Take Hit-sheet.png");
        registerFantasyMonsterSheet(assetRoot, "goblin_death", "Goblin", "Death-sheet.png");
        registerFantasyMonsterSheet(assetRoot, "flying_eye_move", "Flying eye", "Flight-sheet.png");
        registerFantasyMonsterSheet(assetRoot, "flying_eye_attack", "Flying eye", "Attack-sheet.png");
        registerFantasyMonsterSheet(assetRoot, "flying_eye_take_hit", "Flying eye", "Take Hit-sheet.png");
        registerFantasyMonsterSheet(assetRoot, "flying_eye_death", "Flying eye", "Death-sheet.png");
        registerFantasyMonsterSheet(assetRoot, "skeleton_move", "Skeleton", "Walk-sheet.png");
        registerFantasyMonsterSheet(assetRoot, "skeleton_attack", "Skeleton", "Attack-sheet.png");
        registerFantasyMonsterSheet(assetRoot, "skeleton_take_hit", "Skeleton", "Take Hit-sheet.png");
        registerFantasyMonsterSheet(assetRoot, "skeleton_death", "Skeleton", "Death-sheet.png");
        registerFantasyMonsterSheet(assetRoot, "skeleton_shield", "Skeleton", "Shield-sheet.png");
        registerFantasyMonsterSheet(assetRoot, "mushroom_move", "Mushroom", "Run-sheet.png");
        registerFantasyMonsterSheet(assetRoot, "mushroom_idle", "Mushroom", "Idle-sheet.png");
        registerFantasyMonsterSheet(assetRoot, "mushroom_attack", "Mushroom", "Attack-sheet.png");
        registerFantasyMonsterSheet(assetRoot, "mushroom_take_hit", "Mushroom", "Take Hit-sheet.png");
        registerFantasyMonsterSheet(assetRoot, "mushroom_death", "Mushroom", "Death-sheet.png");
        registerImpalerFrameSequence(assetRoot, "idle", "idle", "idle", 4);
        registerImpalerFrameSequence(assetRoot, "move", "walk", "walk", 6);
        registerImpalerFrameSequence(assetRoot, "attack", "attack2", "atk", 8);
        registerImpalerFrameSequence(assetRoot, "death", "death", "dth", 25);
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

    private void registerImpalerFrameSequence(
            Path assetRoot,
            String clipId,
            String assetClipFolder,
            String assetFramePrefix,
            int frameCount
    ) {
        Path clipRoot = assetRoot.resolve(Path.of(
                "boss",
                "Impaler Boss",
                "Impaler Boss",
                assetClipFolder
        ));
        for (int frame = 0; frame < frameCount; frame++) {
            registerTextureIfPresent(
                    impalerTextureId(clipId, frame),
                    clipRoot.resolve(assetFramePrefix + (frame + 1) + ".png")
            );
        }
    }

    private void registerSlimeFrameSequence(
            Path assetRoot,
            String clipId,
            String assetClipFolder,
            String assetFramePrefix,
            int frameCount
    ) {
        Path clipRoot = assetRoot.resolve(Path.of(
                "monster",
                "Slime_Enemy_Pixel_Monsters_Vol_1",
                "Slime_Enemy_Pixel_Monsters_Vol_1",
                "Slime",
                "frames",
                "green",
                assetClipFolder
        ));
        for (int frame = 0; frame < frameCount; frame++) {
            registerTextureIfPresent(
                    slimeTextureId(clipId, frame),
                    clipRoot.resolve(assetFramePrefix + "_" + String.format("%02d", frame) + ".png")
            );
        }
    }

    private String slimeTextureId(String clipId, int frame) {
        return SLIME_TEXTURE_PREFIX + clipId + "_" + String.format("%02d", frame);
    }

    private String impalerTextureId(String clipId, int frame) {
        return IMPALER_TEXTURE_PREFIX + clipId + "_" + String.format("%02d", frame);
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

    private enum EnemyArchetype {
        WALKER,
        FLYER,
        CHARGER
    }

    private static final class BossActor {
        private static final int MAX_HEALTH = 18;

        private final KinematicBody body;
        private final AnimationSetDefinition animationSet;
        private final AnimationPlayer animator = new AnimationPlayer();
        private final HealthPool health = new HealthPool(MAX_HEALTH, 0.12f);
        private final float drawWidth = 360.0f;
        private final float drawHeight = 149.0f;
        private final float drawOffsetX = 110.0f;
        private final float drawOffsetY = -5.0f;
        private final DebugColor fallbackColor = new DebugColor(0.72f, 0.58f, 0.35f, 1.0f);
        private int facingDirection = -1;

        private BossActor(String spawnId, float spawnX, float spawnY, AnimationSetDefinition animationSet) {
            this.body = new KinematicBody(spawnX, spawnY, 58.0f, 92.0f);
            this.animationSet = animationSet;
            this.animator.play(animationSet.clip("idle"));
        }

        private boolean facingRight() {
            if (Math.abs(body.velocityX()) > 0.01f) {
                return body.velocityX() > 0.0f;
            }
            return facingDirection > 0;
        }

        private float drawX() {
            float centeredX = body.x() + body.width() * 0.5f - drawWidth * 0.5f;
            float directionalOffsetX = facingRight() ? -drawOffsetX : drawOffsetX;
            return centeredX + directionalOffsetX;
        }

        private boolean attack2CanReach(Aabb targetBounds) {
            return targetInFacingDirection(targetBounds)
                    && edgeDistanceTo(targetBounds) <= body.width() + BOSS_ATTACK_REACH_PADDING
                    && verticalOverlap(body.bounds(), targetBounds);
        }

        private boolean targetInFacingDirection(Aabb targetBounds) {
            float bodyCenterX = body.x() + body.width() * 0.5f;
            float targetCenterX = targetBounds.x() + targetBounds.width() * 0.5f;
            return facingRight() ? targetCenterX >= bodyCenterX : targetCenterX <= bodyCenterX;
        }

        private float edgeDistanceTo(Aabb targetBounds) {
            if (targetBounds.x() >= body.bounds().right()) {
                return targetBounds.x() - body.bounds().right();
            }
            if (targetBounds.right() <= body.x()) {
                return body.x() - targetBounds.right();
            }
            return 0.0f;
        }

        private boolean verticalOverlap(Aabb first, Aabb second) {
            return first.y() < second.top() && first.top() > second.y();
        }

        private List<HitboxInstance> createAttack2Hitboxes(AttackDefinition attack, int frameIndex) {
            return attack2Bounds(frameIndex).stream()
                    .map(bounds -> new HitboxInstance(BOSS_ENTITY_ID, CombatTeam.ENEMY, attack, bounds, true))
                    .toList();
        }

        private List<Aabb> attack2Bounds(int frameIndex) {
            if (frameIndex >= 1 && frameIndex <= 3) {
                return List.of(facingBodyBox(), topBox());
            }
            if (frameIndex >= 4 && frameIndex <= 6) {
                return List.of(oppositeBodyBox(), facingSmallBox());
            }
            return List.of();
        }

        private Aabb facingBodyBox() {
            return facingRight()
                    ? new Aabb(body.bounds().right(), body.y(), body.width(), body.height())
                    : new Aabb(body.x() - body.width(), body.y(), body.width(), body.height());
        }

        private Aabb oppositeBodyBox() {
            return facingRight()
                    ? new Aabb(body.x() - body.width(), body.y(), body.width(), body.height())
                    : new Aabb(body.bounds().right(), body.y(), body.width(), body.height());
        }

        private Aabb topBox() {
            float width = body.width() / 3.0f;
            float height = body.height() / 3.0f;
            return new Aabb(
                    body.x() + body.width() * 0.5f - width * 0.5f,
                    body.bounds().top(),
                    width,
                    height
            );
        }

        private Aabb facingSmallBox() {
            float width = body.width() / 3.0f;
            float height = body.height() * 0.5f;
            float y = body.y() + body.height() * 0.25f;
            return facingRight()
                    ? new Aabb(body.bounds().right(), y, width, height)
                    : new Aabb(body.x() - width, y, width, height);
        }
    }

    private static final class EnemyActor {
        private final int entityId;
        private final EnemyKind kind;
        private final EnemyArchetype archetype;
        private final String spawnId;
        private final EnemyMovement movement;
        private final float spawnX;
        private final float spawnY;
        private final KinematicBody body;
        private final AnimationSetDefinition animationSet;
        private final HitboxDefinition attackHitboxDefinition;
        private final AnimationPlayer animator = new AnimationPlayer();
        private final PatrolController patrolController;
        private final KinematicMover mover = new KinematicMover();
        private final KinematicImpulseMover knockbackMover = new KinematicImpulseMover();
        private final KinematicImpulseConfig impulseConfig;
        private final EnemyBrain brain;
        private final HealthPool health = new HealthPool(3, 0.0f);
        private final HitStunTimer hitStunTimer = new HitStunTimer();
        private final float flightSpeed;
        private final float chaseSpeed;
        private final float evadeSpeed;
        private final float drawWidth;
        private final float drawHeight;
        private final float drawOffsetX;
        private final float drawOffsetY;
        private final DebugColor fallbackColor;
        private int flightDirection = -1;
        private int facingDirection = -1;
        private float flightAgeSeconds;

        private EnemyActor(
                int entityId,
                EnemyKind kind,
                EnemyArchetype archetype,
                String spawnId,
                EnemyMovement movement,
                float spawnX,
                float spawnY,
                KinematicBody body,
                AnimationSetDefinition animationSet,
                HitboxDefinition attackHitboxDefinition,
                PatrolController patrolController,
                KinematicImpulseConfig impulseConfig,
                float flightSpeed,
                EnemyBrainConfig brainConfig,
                float chaseSpeed,
                float evadeSpeed,
                float drawWidth,
                float drawHeight,
                float drawOffsetX,
                float drawOffsetY,
                DebugColor fallbackColor
        ) {
            this.entityId = entityId;
            this.kind = kind;
            this.archetype = archetype;
            this.spawnId = spawnId;
            this.movement = movement;
            this.spawnX = spawnX;
            this.spawnY = spawnY;
            this.body = body;
            this.animationSet = animationSet;
            this.attackHitboxDefinition = attackHitboxDefinition;
            this.patrolController = patrolController;
            this.impulseConfig = impulseConfig;
            this.flightSpeed = flightSpeed;
            this.brain = new EnemyBrain(brainConfig, entityId * 31 + spawnId.hashCode());
            this.chaseSpeed = chaseSpeed;
            this.evadeSpeed = evadeSpeed;
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
            brain.reset();
            animator.restart(animationSet.clip("move"));
            flightDirection = -1;
            facingDirection = -1;
            flightAgeSeconds = 0.0f;
            if (patrolController != null) {
                patrolController.reset(-1);
            }
        }

        private boolean facingRight() {
            if (Math.abs(body.velocityX()) > 0.01f) {
                return body.velocityX() > 0.0f;
            }
            return facingDirection > 0;
        }

        private float effectiveChaseSpeed() {
            return archetype == EnemyArchetype.CHARGER ? chaseSpeed * 1.35f : chaseSpeed;
        }

        private HitboxInstance createAttackHitbox(AttackDefinition attack) {
            return attackHitboxDefinition.createInstance(
                    entityId,
                    CombatTeam.ENEMY,
                    attack,
                    body.bounds(),
                    facingRight() ? FacingDirection.RIGHT : FacingDirection.LEFT,
                    true
            );
        }

        private boolean usesShieldCaution() {
            return Math.floorMod(entityId, 2) == 1;
        }

        private Aabb attackBounds() {
            return attackHitboxDefinition.createBounds(
                    body.bounds(),
                    facingRight() ? FacingDirection.RIGHT : FacingDirection.LEFT
            );
        }
    }

    private void drawHud() {
        Gdx.graphics.setTitle("Umbra2D | " + room.roomId()
                + " | checkpoint=" + checkpointState.roomId() + ":" + checkpointState.spawnId()
                + " | visited=" + visitedRoomIds.size()
                + " | " + state
                + " | attack=" + attackTimeline.phase()
                + " | hitPause=" + hitPauseTimer.paused()
                + " | playerStun=" + playerHitStunTimer.stunned()
                + " | playerHP=" + playerHealth.currentHealth()
                + " | abilities=" + abilityState.unlockedAbilityIds()
                + " | dash=" + playerDashSeconds
                + " | enemiesAlive=" + aliveEnemyCount()
                + " | boss=" + bossStateLabel()
                + " | selectedAI=" + selectedEnemyState()
                + " | A/D move, Space jump, J attack, Shift/K dash, R reset, Esc quit");
    }

    private String bossStateLabel() {
        if (bossArenaStatus == null) {
            return "none";
        }
        if (boss == null) {
            return bossArenaStatus.state().name();
        }
        return bossArenaStatus.state().name()
                + ":" + bossArenaStatus.phase().id()
                + ":" + boss.health.currentHealth() + "/" + boss.health.maxHealth();
    }

    private String selectedEnemyState() {
        if (selectedEnemy == null || !enemies.contains(selectedEnemy)) {
            return "none";
        }
        return selectedEnemy.brain.state().name();
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
