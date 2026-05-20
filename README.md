# Umbra2D Engine

Focused Java 17 + Maven + LibGDX engine for a room-based Metroidvania action platformer.

The project direction is an internal game-creation toolchain for a specific class of 2D games, not a generic public engine for every genre. See [Internal Game Toolchain Direction](docs/internal-game-toolchain-direction.md).

The first milestone follows the project blueprint: build a playable vertical slice foundation before broader tooling systems. The current implementation starts with a runnable desktop shell, deterministic fixed-step updates, graybox tile collision, a kinematic player controller, and tests.

## Requirements

- JDK 17
- Maven 3.9+

## Build And Test

```powershell
mvn test
```

## Validate Project Content

Use the checked-in helper to validate the sample project manifest before launch:

```powershell
.\scripts\validate-project.ps1
```

If PowerShell script execution is blocked on Windows, use:

```powershell
.\scripts\validate-project.cmd
```

Or run the validator directly:

```powershell
mvn -pl tools-validation -am install "-DskipTests"
mvn -f tools-validation/pom.xml org.codehaus.mojo:exec-maven-plugin:3.2.0:java
```

## Run Desktop Sample

Use the checked-in helper:

```powershell
.\scripts\run-desktop.ps1
```

Or run the two Maven steps manually:

```powershell
mvn -pl desktop -am install "-DskipTests"
mvn -f desktop/pom.xml org.codehaus.mojo:exec-maven-plugin:3.2.0:java
```

Do not run `mvn exec:java` from the repository root; the root project is a parent POM and has no desktop `mainClass`.

Controls:

- `A/D` or arrow keys: move
- `Space` or `Up`: jump
- Press `Space` or `Up` again while airborne after collecting the double-jump pickup
- `Shift` or `K`: dash after collecting the dash pickup
- `J`: attack
- `R`: reset player, enemy, and combat state
- Walk into cyan door triggers: move between test rooms
- Walk through green checkpoint triggers: update death respawn point and write local sample save
- Walk into the blue ability pickups: unlock dash or double jump and persist them in the local sample save
- Red ability gates block the player until the required ability is unlocked
- `Esc`: quit
- Left-side editor buttons: add slime, goblin, flying eye, skeleton, or mushroom near the player
- Click an enemy body, then click `Remove Selected`: remove that specific enemy from the room

## Asset Policy

Raw art assets live outside this implementation repo in `../assets`. The repository commits only metadata/configuration until the asset set is cleaned, validated, and license-checked.

Initial source asset mapping:

- Knight: `../assets/char/FreeKnight_v1/Colour1/NoOutline/120x80_PNGSheets`
- Mossy tiles: `../assets/tiles/Mossy Tileset`
- Slimes: `../assets/monster/Slime_Enemy_Pixel_Monsters_Vol_1/Slime_Enemy_Pixel_Monsters_Vol_1/Slime/frames`
- Fantasy creatures: `../assets/monster/Monsters_Creatures_Fantasy/Monsters_Creatures_Fantasy`

Do not commit local absolute paths, credentials, generated build folders, IDE state, or raw asset archives unless licensing and packaging are explicitly approved.

## Architecture Snapshot

- `engine-core`: config, scene lifecycle, fixed timestep.
- `engine-render`: debug draw command model, sprite draw command model, and LibGDX render adapters.
- `engine-ai`: deterministic enemy sight, chase, cautious attack preparation, attack, evade, hit-stun, and dead state decisions.
- `engine-physics2d`: tile collision, kinematic player movement, simple enemy patrol movement, and impulse/knockback movement.
- `engine-assets`: asset path validation.
- `engine-room`: JSON room model loading and v1 contract validation.
- `engine-combat`: hitbox/hurtbox definitions and overlap resolution, team filtering, attack timing, damage events, health, invulnerability frames, hit pause, and hit stun.
- `engine-progression`: unlocked ability state and ability pickup/gate trigger resolution.
- `engine-project`: project manifest loading and validation for title, start room, default spawn, asset root, save policy, and enabled modules.
- `tools-validation`: command-line project validation entry point.
- `sample-metroidvania`: LibGDX graybox room proving movement and collision.
- `desktop`: LWJGL3 launcher.

## Current Scope

Implemented first because it blocks every later gameplay feature:

- Java 17 Maven project structure.
- Fixed-step scene runtime.
- Renderer command layers for sprites, grids, solid tiles, and AABB overlays.
- 32 px tile collision grid.
- Kinematic player controller with acceleration, gravity, jump cut, coyote time, and jump buffer.
- Kinematic patrol controller for ground enemies with wall and ledge reversal.
- Kinematic impulse mover for knockback and hit-stun movement through tile collision.
- Data-loaded debug-visible graybox room.
- Room validation for 32 px tile size, solid tiles, player spawns, doors, isolated rooms, and camera zones.
- Facing-aware hitbox definitions and active hitbox versus enabled hurtbox damage events with optional team filtering.
- Attack timeline player for startup, active, recovery, and finished windows.
- Health pools with deterministic invulnerability-frame rejection.
- Deterministic hit-pause timer for impact freeze frames.
- Deterministic hit-stun timer for post-hit action lock.
- Metadata-driven animation clips for sheet-based sprites and image-sequence sprites.
- Animated sample sprites for player idle/run/jump/fall/attack/hit/death; slime idle/move/slide/attack/take-hit/death; and fantasy monster move/attack/take-hit/death clips where source assets provide them.
- Skeleton shield animation metadata is registered for the next guard-behavior increment.
- Enemy AI vision behavior: patrol outside sight, chase visible player, become cautious at close range, attack after windup, and probabilistically evade nearby player attacks.
- Enemy attack damage uses facing-aware attack hitboxes: goblin/skeleton weapon enemies reach about one player-width forward, while slime/flying eye/mushroom use smaller close-range hitboxes.
- Animation attack timing can be driven by frame events in animation metadata.
- Door and checkpoint triggers are visible in the debug room layer.
- The sample can transition between two test rooms and respawn the player at the latest checkpoint after death or `R`.
- Checkpoint and visited-room save data is serialized by `engine-save` and loaded on sample startup from `.umbra2d/sample-save.json`.
- Ability unlock state is handled by `engine-progression` and serialized by `engine-save`; the sample dash and double-jump pickups persist across restart and gates read that state.
- The sample reads `sample-metroidvania/src/main/resources/game.manifest.json` for project title, startup room, default spawn, asset root, save policy, and enabled engine modules.
- Creature authoring data exists for slime, goblin, flying eye, skeleton, and mushroom under `sample-metroidvania/src/main/resources/metadata/creatures`; validation checks their state mappings against animation metadata before launch.
- Graybox combat sample with player slash, multiple moving enemies, anchored hitbox movement, contact damage, knockback, HP state, hit pause, hit stun, active hitbox debug, enemy hurtboxes, and reset.

Sprite assets are loaded from `../assets` by default. Override with:

```powershell
mvn -f desktop/pom.xml "-Dumbra.assets.root=C:\path\to\assets" org.codehaus.mojo:exec-maven-plugin:3.2.0:java
```

If sprite files are unavailable, the sample falls back to colored debug rectangles.

Next planned systems are deeper asset import validation and richer room-to-room transitions.
