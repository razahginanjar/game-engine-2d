# Internal Game Toolchain Direction

## Product Direction

Umbra2D is an internal Java 17, Maven, and LibGDX game-creation toolchain for a specific class of 2D games: room-based action platformers and Metroidvania-style projects.

The engine is not trying to become a general public engine for every 2D genre. Its purpose is to help an internal game team build this kind of game faster through reusable runtime systems, data-driven content, validation tools, and starter project templates.

## Target User

The primary user is an internal developer or technical designer who needs to:

- Create rooms, exits, checkpoints, ability gates, enemies, bosses, and pickups.
- Configure gameplay content without editing core engine code.
- Validate assets, metadata, and room data before runtime.
- Run a sample or generated game quickly from a clean checkout.
- Package a playable build using a repeatable workflow.

## Non-Goals

- Do not support every 2D game genre in v1.
- Do not expose unstable engine internals as public APIs.
- Do not prioritize external marketplace-style distribution before the internal workflow works.
- Do not commit raw art assets until licensing and packaging policy is complete.

## Roadmap Adjustment

The original phase roadmap remains valid through Phase 10, but the project should add an internal workflow hardening milestone before final packaging.

Current phase sequence:

- Phase 10: Boss arena framework.
- Phase 10.5: Internal game creation workflow.
- Phase 11: UI, HUD, map, pause, settings, and input rebinding.
- Phase 12: Packaging, docs, tests, sample project, and release gates.

Phase 10.5 should exist because the goal is not only to make one sample game. The goal is to make a toolchain that helps the team create this style of 2D game repeatedly.

## Phase 10.5 Scope

Phase 10.5 should deliver:

- Game manifest: a single project-level config that declares title, start room, asset root, save policy, and enabled modules.
- Starter template: a clean generated game project separate from the engine internals.
- Content schemas: documented JSON contracts for rooms, enemies, attacks, abilities, bosses, pickups, gates, saves, and UI keys.
- Validation CLI: a command to validate project content before launch.
- Import reports: asset metadata checks for sprite dimensions, frame counts, animation events, pivots, colliders, and license notes.
- Authoring docs: internal guides for adding a room, enemy, ability, boss, and checkpoint.

## Architectural Rule

Sample code may prove a feature first, but stable behavior must move into engine modules once the behavior is understood.

Examples:

- Ability state and gates belong in `engine-progression`.
- Combat timing and hitboxes belong in `engine-combat`.
- Room contracts and transitions belong in `engine-room`.
- Boss state and arena locks should become an engine module during Phase 10, not remain only in `TestRoomScene`.

## Success Criteria

The internal toolchain direction is successful when a developer can create a small new Metroidvania-style prototype by:

- Generating or copying a starter project.
- Adding room JSON and metadata.
- Validating the project with one command.
- Running the game without changing engine internals.
- Packaging a playable desktop build.

