# Internal Game Toolchain Direction

## Product Direction

Umbra2D is an internal Java 17, Maven, and LibGDX game-creation toolchain for a specific class of 2D games: room-based action platformers and Metroidvania-style projects.

The engine is not trying to become a general public engine for every 2D genre. Its purpose is to help an internal game team build this kind of game faster through reusable runtime systems, data-driven content, validation tools, and starter project templates.

## Target User

The primary user is an internal developer or technical designer who needs to:

- Create rooms, exits, checkpoints, ability gates, enemies, bosses, and pickups.
- Create or modify playable characters, monsters, NPCs, and bosses from data-driven definitions.
- Change room backgrounds, foreground layers, parallax, tint, and ambience through an engine/editor preview instead of hardcoded Java setup.
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
- Content schemas: documented JSON contracts for rooms, room visuals, backgrounds, parallax layers, enemies, attacks, abilities, bosses, pickups, gates, saves, and UI keys.
- Character and creature authoring: data-driven definitions for playable characters, monsters, NPCs, and bosses, including allowed states, optional states, animation mapping, combat data, AI profile, and validation rules.
- Room visual authoring: editable per-room background and foreground configuration with layer order, parallax, repeat mode, offset, scale, tint, opacity, ambient color, and saved room metadata.
- Validation CLI: a command to validate project content before launch.
- Import reports: asset metadata checks for sprite dimensions, frame counts, animation events, pivots, colliders, background dimensions, parallax suitability, and license notes.
- Authoring docs: internal guides for adding a room, background layer, enemy, ability, boss, checkpoint, and packaged build.

## Character And Creature Authoring

The engine should allow a developer to add a new creature such as `troll` without editing core engine code for normal content work.

A creature definition should declare:

- Identity: id, display name, category, asset root, and optional tags.
- Physical setup: body bounds, hurtbox, contact damage, movement speed, gravity/flying behavior, and collision profile.
- AI setup: patrol, vision range, caution range, attack range, evade probability, shield usage, pursuit rules, and allowed transitions.
- State model: required states, optional states, disabled states, fallback states, and transition rules.
- Animation mapping: clip ids for states such as idle, walk, run, fly, cautious, attack, shield, hurt, death, and special.
- Combat timing: attack hitboxes, active frames, windup frames, recovery frames, damage, knockback, and cooldowns.
- Rewards and persistence: drops, defeat flags, respawn rules, and world-state updates.

Optional states are valid when the creature cannot enter them. For example, a troll with no shield animation may omit `shield` only if its AI profile never transitions into shield. Combat-critical states must be explicit; attack active frames and death behavior should not silently fall back to idle.

The validator should fail clearly when:

- A required state has no animation.
- An AI transition references a disabled or missing state.
- Attack active frames reference missing sprite frames.
- Sprite orientation metadata conflicts with runtime facing logic.
- A creature references assets outside the approved asset root.

## Room Visual And Background Authoring

Room backgrounds should be configured through data and previewed inside the engine/editor workflow. A developer should not need to hardcode the background before starting the engine.

The workflow should allow a developer or technical designer to:

- Open a room in preview mode.
- Choose an approved background asset from an asset browser.
- Add multiple background or foreground layers.
- Adjust scale, offset, opacity, tint, repeat mode, scroll lock, and parallax factor.
- Preview camera movement to verify the background covers the playable area.
- Toggle collision, trigger, AI, and render-layer overlays while tuning visuals.
- Save the final configuration back to room JSON or room metadata.

Room visual configuration belongs to room data, not Java scene code. The sample game may provide defaults, but production rooms should load visual settings from data.

The validator should report missing background ids, invalid layer order, unsupported repeat modes, bad parallax values, oversized textures, backgrounds that do not cover camera bounds, and references to assets outside the approved asset root.

## Architectural Rule

Sample code may prove a feature first, but stable behavior must move into engine modules once the behavior is understood.

Examples:

- Ability state and gates belong in `engine-progression`.
- Combat timing and hitboxes belong in `engine-combat`.
- Room contracts and transitions belong in `engine-room`.
- Room visual contracts belong in `engine-room`; layered drawing, parallax, tint, and debug visualization belong in `engine-render`.
- Character state definitions are content data; reusable AI state behavior belongs in `engine-ai`; animation state mapping belongs in `engine-animation`.
- Boss state and arena locks should become an engine module during Phase 10, not remain only in `TestRoomScene`.

## Success Criteria

The internal toolchain direction is successful when a developer can create a small new Metroidvania-style prototype by:

- Generating or copying a starter project.
- Adding room JSON and metadata.
- Adding or editing room background data without Java scene changes.
- Adding a new creature definition with only the states and animations that creature supports.
- Validating the project with one command.
- Running the game without changing engine internals.
- Packaging a playable desktop build.
