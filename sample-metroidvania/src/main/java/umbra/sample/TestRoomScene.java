package umbra.sample;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import umbra.animation.AnimationClipDefinition;
import umbra.animation.AnimationMetadataLoader;
import umbra.animation.AnimationPlayer;
import umbra.animation.AnimationSetDefinition;
import umbra.core.EngineConfig;
import umbra.core.Scene;
import umbra.physics.CollisionGrid;
import umbra.physics.KinematicBody;
import umbra.physics.player.PlayerController;
import umbra.physics.player.PlayerControllerConfig;
import umbra.physics.player.PlayerInput;
import umbra.physics.player.PlayerState;
import umbra.room.RoomDefinition;
import umbra.room.RoomLoader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

final class TestRoomScene implements Scene {
    private final ShapeRenderer shapes;
    private final SpriteBatch batch;
    private final Camera camera;
    private final EngineConfig config;
    private final RoomDefinition room;
    private final CollisionGrid grid;
    private final KinematicBody player;
    private final PlayerController controller;
    private final AnimationSetDefinition playerAnimations;
    private final AnimationPlayer animationPlayer = new AnimationPlayer();
    private final Map<String, Texture> clipTextures = new HashMap<>();
    private final boolean renderPlayerSprites;
    private boolean previousJumpDown;
    private boolean facingRight = true;
    private PlayerState state = PlayerState.IDLE;

    TestRoomScene(ShapeRenderer shapes, SpriteBatch batch, Camera camera, EngineConfig config) {
        this.shapes = shapes;
        this.batch = batch;
        this.camera = camera;
        this.config = config;
        this.room = loadRoom();
        this.grid = createGrid(room);
        this.playerAnimations = loadPlayerAnimations();
        this.renderPlayerSprites = tryLoadPlayerTextures();
        animationPlayer.play(playerAnimations.clip("idle").orElseThrow());
        RoomDefinition.SpawnPoint playerSpawn = room.spawns().stream()
                .filter(spawn -> spawn.type().equals("player_spawn"))
                .findFirst()
                .orElseThrow();
        this.player = new KinematicBody(playerSpawn.x(), playerSpawn.y(), 18.0f, 38.0f);
        this.controller = new PlayerController(PlayerControllerConfig.metroidvaniaDefaults());
    }

    @Override
    public void update(float deltaSeconds) {
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
        }

        state = controller.update(input, player, grid, deltaSeconds);
        animationPlayer.play(playerAnimations.clip(animationIdFor(state)).orElseThrow());
        animationPlayer.update(deltaSeconds);
        clampCameraToRoom();
    }

    @Override
    public void render() {
        drawRoom();
        drawPlayer();
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

    private AnimationSetDefinition loadPlayerAnimations() {
        try (InputStreamReader reader = new InputStreamReader(
                Objects.requireNonNull(TestRoomScene.class.getResourceAsStream("/metadata/player_knight.anim.json")),
                StandardCharsets.UTF_8
        )) {
            return new AnimationMetadataLoader().load(reader);
        } catch (IOException exception) {
            throw new IllegalStateException("failed to close animation metadata resource", exception);
        }
    }

    private boolean tryLoadPlayerTextures() {
        Path assetRoot = Path.of(System.getProperty("umbra.assets.root", "../assets")).toAbsolutePath().normalize();
        AnimationMetadataLoader loader = new AnimationMetadataLoader();
        try {
            loader.validateSheets(playerAnimations, assetRoot, Set.of("idle", "run", "jump", "fall", "dash", "attack", "hit", "death"));
            for (AnimationClipDefinition clip : playerAnimations.clips()) {
                Texture texture = new Texture(Gdx.files.absolute(assetRoot.resolve(clip.sheetPath()).toString()));
                texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                clipTextures.put(clip.id(), texture);
            }
            return true;
        } catch (RuntimeException exception) {
            for (Texture texture : clipTextures.values()) {
                texture.dispose();
            }
            clipTextures.clear();
            Gdx.app.log("Umbra2D", "Player sprite sheets unavailable, using debug collider: " + exception.getMessage());
            return false;
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
        if (renderPlayerSprites) {
            drawPlayerSprite();
        } else {
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(playerColor());
            shapes.rect(player.x(), player.y(), player.width(), player.height());
            shapes.end();
        }

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.YELLOW);
        shapes.rect(player.x(), player.y(), player.width(), player.height());
        shapes.end();
    }

    private void drawPlayerSprite() {
        AnimationClipDefinition clip = animationPlayer.clip();
        Texture texture = clipTextures.get(clip.id());
        int srcX = animationPlayer.frameIndex() * playerAnimations.frameWidth();
        int srcY = 0;
        float drawX = player.x() - playerAnimations.collisionBox().x();
        float drawY = player.y() - playerAnimations.collisionBox().y();

        batch.begin();
        batch.draw(
                texture,
                drawX,
                drawY,
                playerAnimations.frameWidth(),
                playerAnimations.frameHeight(),
                srcX,
                srcY,
                playerAnimations.frameWidth(),
                playerAnimations.frameHeight(),
                !facingRight,
                false
        );
        batch.end();
    }

    private Color playerColor() {
        return switch (state) {
            case IDLE -> Color.SKY;
            case RUN -> Color.CYAN;
            case JUMP -> Color.LIME;
            case FALL -> Color.ORANGE;
        };
    }

    private void drawHud() {
        Gdx.graphics.setTitle("Umbra2D | " + room.roomId() + " | " + animationIdFor(state) + " | A/D move, Space jump, R reset, Esc quit");
    }

    @Override
    public void dispose() {
        for (Texture texture : clipTextures.values()) {
            texture.dispose();
        }
        clipTextures.clear();
    }

    private String animationIdFor(PlayerState state) {
        return switch (state) {
            case IDLE -> "idle";
            case RUN -> "run";
            case JUMP -> "jump";
            case FALL -> "fall";
        };
    }
}
