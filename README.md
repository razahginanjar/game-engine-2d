# Umbra2D Engine

Focused Java 17 + Maven + LibGDX engine for a room-based Metroidvania action platformer.

The first milestone follows the project blueprint: build a playable vertical slice foundation before generic engine systems. The current implementation starts with a runnable desktop shell, deterministic fixed-step updates, graybox tile collision, a kinematic player controller, and tests.

## Requirements

- JDK 17
- Maven 3.9+

## Build And Test

```powershell
mvn test
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
- `J`: attack
- `R`: reset player, enemy, and combat state
- `Esc`: quit

## Asset Policy

Raw art assets live outside this implementation repo in `../assets`. The repository commits only metadata/configuration until the asset set is cleaned, validated, and license-checked.

Initial source asset mapping:

- Knight: `../assets/char/assassin-mage-viking-free-pixel-art-game-heroes/PNG/Knight`
- Mossy tiles: `../assets/tiles/Mossy Tileset`
- Slimes: `../assets/monster/Slimes`

Do not commit local absolute paths, credentials, generated build folders, IDE state, or raw asset archives unless licensing and packaging are explicitly approved.

## Architecture Snapshot

- `engine-core`: config, scene lifecycle, fixed timestep.
- `engine-physics2d`: tile collision and kinematic player movement.
- `engine-assets`: asset path validation.
- `engine-room`: JSON room model loading and validation.
- `engine-combat`: hitbox/hurtbox overlap resolution, team filtering, attack timing, damage events, health, invulnerability frames, and hit pause.
- `sample-metroidvania`: LibGDX graybox room proving movement and collision.
- `desktop`: LWJGL3 launcher.

## Current Scope

Implemented first because it blocks every later gameplay feature:

- Java 17 Maven project structure.
- Fixed-step scene runtime.
- 32 px tile collision grid.
- Kinematic player controller with acceleration, gravity, jump cut, coyote time, and jump buffer.
- Data-loaded debug-visible graybox room.
- Combat resolver for active hitbox versus enabled hurtbox damage events with optional team filtering.
- Attack timeline player for startup, active, recovery, and finished windows.
- Health pools with deterministic invulnerability-frame rejection.
- Deterministic hit-pause timer for impact freeze frames.
- Graybox combat sample with player slash, contact damage, knockback, HP state, hit pause, active hitbox debug, slime hurtbox, and reset.

Next planned systems are metadata-driven asset import and animation state machine.
