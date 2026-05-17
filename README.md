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

```powershell
mvn -pl desktop -am exec:java
```

If the external art folder is not adjacent to this repo, override it:

```powershell
mvn -pl desktop -am exec:java -Dumbra.assets.root="C:\path\to\assets"
```

Controls:

- `A/D` or arrow keys: move
- `Space` or `Up`: jump
- `R`: reset player
- `Esc`: quit

## Asset Policy

Raw art assets live outside this implementation repo in `../assets`. The repository commits only metadata/configuration until the asset set is cleaned, validated, and license-checked.

Initial source asset mapping:

- Player knight sheets: `../assets/char/FreeKnight_v1/Colour1/NoOutline/120x80_PNGSheets`
- Mossy tiles: `../assets/tiles/Mossy Tileset`
- Slimes: `../assets/monster/Slimes`

Do not commit local absolute paths, credentials, generated build folders, IDE state, or raw asset archives unless licensing and packaging are explicitly approved.

## Architecture Snapshot

- `engine-core`: config, scene lifecycle, fixed timestep.
- `engine-physics2d`: tile collision and kinematic player movement.
- `engine-assets`: asset path validation.
- `engine-room`: JSON room model loading and validation.
- `engine-animation`: metadata-driven sprite sheet animation validation.
- `sample-metroidvania`: LibGDX graybox room proving movement and collision.
- `desktop`: LWJGL3 launcher.

## Current Scope

Implemented first because it blocks every later gameplay feature:

- Java 17 Maven project structure.
- Fixed-step scene runtime.
- 32 px tile collision grid.
- Kinematic player controller with acceleration, gravity, jump cut, coyote time, and jump buffer.
- Data-loaded debug-visible graybox room.
- Player animation metadata for required MVP clips: idle, run, jump, fall, dash, attack, hit, death.
- Deterministic animation clip playback for looped and one-shot clips.

Next planned systems are player animation rendering/state selection and combat hitbox/hurtbox timing.
