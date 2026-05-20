package umbra.project;

import umbra.animation.AnimationClipDefinition;
import umbra.animation.AnimationMetadataLoader;
import umbra.animation.AnimationSetDefinition;
import umbra.boss.BossDefinition;
import umbra.boss.BossDefinitionLoader;
import umbra.room.RoomDefinition;
import umbra.room.RoomLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ProjectValidator {
    private static final Set<String> KNOWN_MODULES = Set.of(
            "ai",
            "animation",
            "assets",
            "boss",
            "combat",
            "physics2d",
            "progression",
            "project",
            "render",
            "room",
            "save"
    );

    private final GameManifestLoader manifestLoader = new GameManifestLoader();
    private final CreatureDefinitionLoader creatureLoader = new CreatureDefinitionLoader();
    private final RoomVisualDefinitionLoader roomVisualLoader = new RoomVisualDefinitionLoader();
    private final RoomLoader roomLoader = new RoomLoader();
    private final BossDefinitionLoader bossLoader = new BossDefinitionLoader();

    public ProjectValidationReport validate(Path projectRoot, String manifestPath) {
        Path absoluteRoot = projectRoot.toAbsolutePath().normalize();
        List<ProjectValidationIssue> issues = new ArrayList<>();
        GameManifest manifest;
        try {
            manifest = manifestLoader.loadProjectManifest(absoluteRoot, manifestPath);
        } catch (RuntimeException exception) {
            issues.add(error("manifest.invalid", exception.getMessage()));
            return new ProjectValidationReport(Optional.empty(), issues);
        }

        validateAssetRoot(absoluteRoot, manifest, issues);
        validateSavePolicy(absoluteRoot, manifest, issues);
        validateEnabledModules(manifest, issues);
        Map<String, RoomDefinition> rooms = validateRoomDefinitions(absoluteRoot, manifest, issues);
        Set<String> bossIds = validateBossDefinitions(absoluteRoot, manifest, issues);
        validateBossArenaReferences(rooms, bossIds, issues);
        validateCreatureDefinitions(absoluteRoot, manifest, issues);
        validateRoomVisualDefinitions(absoluteRoot, manifest, rooms.keySet(), issues);
        return new ProjectValidationReport(Optional.of(manifest), issues);
    }

    private void validateAssetRoot(Path projectRoot, GameManifest manifest, List<ProjectValidationIssue> issues) {
        Path assetRoot = projectRoot.resolve(manifest.assetRoot()).normalize();
        if (!Files.exists(assetRoot)) {
            issues.add(warning("asset_root.missing", "asset_root does not exist: " + assetRoot));
            return;
        }
        if (!Files.isDirectory(assetRoot)) {
            issues.add(error("asset_root.not_directory", "asset_root is not a directory: " + assetRoot));
        }
    }

    private void validateSavePolicy(Path projectRoot, GameManifest manifest, List<ProjectValidationIssue> issues) {
        SavePolicy savePolicy = manifest.savePolicy();
        if (!savePolicy.enabled()) {
            return;
        }
        Path savePath = Path.of(savePolicy.path());
        if (savePath.isAbsolute()) {
            issues.add(error("save.path.absolute", "save.path must be local to the project: " + savePolicy.path()));
            return;
        }
        Path normalizedSavePath = projectRoot.resolve(savePath).normalize();
        if (!normalizedSavePath.startsWith(projectRoot)) {
            issues.add(error("save.path.escapes_project", "save.path escapes the project root: " + savePolicy.path()));
        }
    }

    private void validateEnabledModules(GameManifest manifest, List<ProjectValidationIssue> issues) {
        for (String module : manifest.enabledModules()) {
            if (!KNOWN_MODULES.contains(module)) {
                issues.add(error("module.unknown", "enabled_modules contains unknown module: " + module));
            }
        }
    }

    private Map<String, RoomDefinition> validateRoomDefinitions(
            Path projectRoot,
            GameManifest manifest,
            List<ProjectValidationIssue> issues
    ) {
        Map<String, RoomDefinition> rooms = new HashMap<>();
        for (String definitionPath : manifest.roomDefinitions()) {
            RoomDefinition room;
            try {
                room = loadRoom(projectRoot, definitionPath);
            } catch (RuntimeException exception) {
                issues.add(error("room.invalid", "invalid room definition " + definitionPath + ": " + exception.getMessage()));
                continue;
            }
            RoomDefinition previous = rooms.put(room.roomId(), room);
            if (previous != null) {
                issues.add(error("room.duplicate_id", "duplicate room id: " + room.roomId()));
            }
        }
        if (!rooms.isEmpty()) {
            validateStartRoom(manifest, rooms, issues);
            validateDoorTargets(rooms, issues);
        }
        return rooms;
    }

    private void validateStartRoom(
            GameManifest manifest,
            Map<String, RoomDefinition> rooms,
            List<ProjectValidationIssue> issues
    ) {
        RoomDefinition startRoom = rooms.get(manifest.startRoomId());
        if (startRoom == null) {
            issues.add(error("manifest.start_room.missing",
                    "start_room_id is not listed in room_definitions: " + manifest.startRoomId()));
            return;
        }
        boolean hasDefaultSpawn = startRoom.spawns().stream()
                .anyMatch(spawn -> spawn.id().equals(manifest.defaultSpawnId()));
        if (!hasDefaultSpawn) {
            issues.add(error("manifest.default_spawn.missing",
                    "default_spawn_id is missing from start room: " + manifest.defaultSpawnId()));
        }
    }

    private void validateDoorTargets(Map<String, RoomDefinition> rooms, List<ProjectValidationIssue> issues) {
        for (RoomDefinition room : rooms.values()) {
            for (RoomDefinition.DoorDefinition door : room.doors()) {
                if ("self".equals(door.targetRoom())) {
                    continue;
                }
                RoomDefinition targetRoom = rooms.get(door.targetRoom());
                if (targetRoom == null) {
                    issues.add(error("room.door.target_room_missing",
                            "door targets room not listed in room_definitions: " + room.roomId() + "." + door.id()));
                    continue;
                }
                boolean hasTargetSpawn = targetRoom.spawns().stream()
                        .anyMatch(spawn -> spawn.id().equals(door.targetSpawn()));
                if (!hasTargetSpawn) {
                    issues.add(error("room.door.target_spawn_missing",
                            "door targets missing spawn: " + room.roomId() + "." + door.id()
                                    + " -> " + door.targetRoom() + "." + door.targetSpawn()));
                }
            }
        }
    }

    private Set<String> validateBossDefinitions(Path projectRoot, GameManifest manifest, List<ProjectValidationIssue> issues) {
        Set<String> bossIds = new HashSet<>();
        for (String definitionPath : manifest.bossDefinitions()) {
            BossDefinition boss;
            try {
                boss = loadBoss(projectRoot, definitionPath);
            } catch (RuntimeException exception) {
                issues.add(error("boss.invalid", "invalid boss definition " + definitionPath + ": " + exception.getMessage()));
                continue;
            }
            if (!bossIds.add(boss.id())) {
                issues.add(error("boss.duplicate_id", "duplicate boss id: " + boss.id()));
            }
        }
        return bossIds;
    }

    private void validateBossArenaReferences(
            Map<String, RoomDefinition> rooms,
            Set<String> bossIds,
            List<ProjectValidationIssue> issues
    ) {
        if (rooms.isEmpty() || bossIds.isEmpty()) {
            return;
        }
        for (RoomDefinition room : rooms.values()) {
            for (RoomDefinition.BossArenaDefinition arena : room.bossArenas()) {
                if (!bossIds.contains(arena.bossId())) {
                    issues.add(error("room.boss_arena.boss_missing",
                            "boss arena references boss not listed in boss_definitions: " + room.roomId() + "." + arena.id()));
                }
            }
        }
    }

    private void validateCreatureDefinitions(Path projectRoot, GameManifest manifest, List<ProjectValidationIssue> issues) {
        Path approvedAssetRoot = projectRoot.resolve(manifest.assetRoot()).normalize();
        Set<String> creatureIds = new HashSet<>();
        for (String definitionPath : manifest.creatureDefinitions()) {
            CreatureDefinition creature;
            try {
                creature = creatureLoader.loadProjectCreature(projectRoot, definitionPath);
            } catch (RuntimeException exception) {
                issues.add(error("creature.invalid", "invalid creature definition " + definitionPath + ": " + exception.getMessage()));
                continue;
            }

            if (!creatureIds.add(creature.id())) {
                issues.add(error("creature.duplicate_id", "duplicate creature id: " + creature.id()));
            }
            validateCreatureAssetRoot(approvedAssetRoot, creature, issues);
            validateCreatureAnimation(projectRoot, creature, issues);
        }
    }

    private void validateCreatureAssetRoot(
            Path approvedAssetRoot,
            CreatureDefinition creature,
            List<ProjectValidationIssue> issues
    ) {
        Path creatureAssetRoot = approvedAssetRoot.resolve(creature.assetRoot()).normalize();
        if (!creatureAssetRoot.startsWith(approvedAssetRoot)) {
            issues.add(error("creature.asset_root.escapes_assets",
                    "creature asset_root escapes approved asset root: " + creature.id()));
        }
    }

    private void validateCreatureAnimation(
            Path projectRoot,
            CreatureDefinition creature,
            List<ProjectValidationIssue> issues
    ) {
        AnimationSetDefinition animationSet;
        try {
            animationSet = loadAnimationSet(projectRoot, creature.animationMetadata());
        } catch (RuntimeException exception) {
            issues.add(error("creature.animation.invalid",
                    "invalid animation metadata for creature " + creature.id() + ": " + exception.getMessage()));
            return;
        }

        CreatureStateModel stateModel = creature.states();
        for (String state : stateModel.requiredStates()) {
            String clipId = stateModel.animationMapping().get(state);
            if (clipId == null || clipId.isBlank()) {
                issues.add(error("creature.state.missing_mapping",
                        "required state has no animation mapping: " + creature.id() + "." + state));
                continue;
            }
            if (!animationSet.hasClip(clipId)) {
                issues.add(error("creature.state.missing_clip",
                        "state maps to missing animation clip: " + creature.id() + "." + state + " -> " + clipId));
            }
        }
        for (String clipId : stateModel.animationMapping().values()) {
            if (!animationSet.hasClip(clipId)) {
                issues.add(error("creature.state.missing_clip",
                        "animation mapping references missing clip: " + creature.id() + " -> " + clipId));
            }
        }
        validateCreatureAttackEvent(creature, animationSet, issues);
    }

    private void validateCreatureAttackEvent(
            CreatureDefinition creature,
            AnimationSetDefinition animationSet,
            List<ProjectValidationIssue> issues
    ) {
        if (creature.combat().attackDamage() <= 0) {
            return;
        }
        String attackClipId = creature.states().animationMapping().get("attack");
        if (attackClipId == null || !animationSet.hasClip(attackClipId)) {
            return;
        }
        AnimationClipDefinition attackClip = animationSet.clip(attackClipId);
        boolean hasActiveEvent = attackClip.events().stream()
                .anyMatch(event -> creature.combat().attackActiveEvent().equals(event.id()));
        if (!hasActiveEvent) {
            issues.add(error("creature.attack.missing_active_event",
                    "attack clip has no active event " + creature.combat().attackActiveEvent() + ": " + creature.id()));
        }
    }

    private AnimationSetDefinition loadAnimationSet(Path projectRoot, String relativePath) {
        Path animationPath = projectRoot.resolve(relativePath).normalize();
        if (!animationPath.startsWith(projectRoot)) {
            throw new CreatureDefinitionValidationException("animation_metadata escapes project root: " + relativePath);
        }
        try (Reader reader = Files.newBufferedReader(animationPath)) {
            return new AnimationMetadataLoader().load(reader);
        } catch (IOException exception) {
            throw new CreatureDefinitionValidationException("failed to read animation metadata: " + relativePath, exception);
        }
    }

    private void validateRoomVisualDefinitions(
            Path projectRoot,
            GameManifest manifest,
            Set<String> knownRoomIds,
            List<ProjectValidationIssue> issues
    ) {
        Path approvedAssetRoot = projectRoot.resolve(manifest.assetRoot()).normalize();
        Set<String> visualRoomIds = new HashSet<>();
        for (String definitionPath : manifest.roomVisualDefinitions()) {
            RoomVisualDefinition visual;
            try {
                visual = roomVisualLoader.loadProjectRoomVisual(projectRoot, definitionPath);
            } catch (RuntimeException exception) {
                issues.add(error("room_visual.invalid",
                        "invalid room visual definition " + definitionPath + ": " + exception.getMessage()));
                continue;
            }
            if (!visualRoomIds.add(visual.roomId())) {
                issues.add(error("room_visual.duplicate_room", "duplicate room visual definition: " + visual.roomId()));
            }
            if (!knownRoomIds.isEmpty() && !knownRoomIds.contains(visual.roomId())) {
                issues.add(error("room_visual.room_missing",
                        "room visual references room not listed in room_definitions: " + visual.roomId()));
            }
            validateRoomVisualAssets(approvedAssetRoot, visual, issues);
        }
    }

    private void validateRoomVisualAssets(
            Path approvedAssetRoot,
            RoomVisualDefinition visual,
            List<ProjectValidationIssue> issues
    ) {
        for (RoomVisualLayerDefinition layer : visual.layers()) {
            Path assetPath = approvedAssetRoot.resolve(layer.assetPath()).normalize();
            if (!assetPath.startsWith(approvedAssetRoot)) {
                issues.add(error("room_visual.asset.escapes_assets",
                        "room visual layer asset escapes approved asset root: " + visual.roomId() + "." + layer.id()));
                continue;
            }
            if (!Files.exists(assetPath)) {
                issues.add(warning("room_visual.asset.missing",
                        "room visual layer asset does not exist: " + visual.roomId() + "." + layer.id()));
            }
        }
    }

    private static ProjectValidationIssue error(String code, String message) {
        return new ProjectValidationIssue(ProjectValidationSeverity.ERROR, code, message);
    }

    private static ProjectValidationIssue warning(String code, String message) {
        return new ProjectValidationIssue(ProjectValidationSeverity.WARNING, code, message);
    }

    private RoomDefinition loadRoom(Path projectRoot, String relativePath) {
        Path roomPath = projectRoot.resolve(relativePath).normalize();
        if (!roomPath.startsWith(projectRoot)) {
            throw new GameManifestValidationException("room definition escapes project root: " + relativePath);
        }
        try (Reader reader = Files.newBufferedReader(roomPath)) {
            return roomLoader.load(reader);
        } catch (IOException exception) {
            throw new GameManifestValidationException("failed to read room definition: " + relativePath, exception);
        }
    }

    private BossDefinition loadBoss(Path projectRoot, String relativePath) {
        Path bossPath = projectRoot.resolve(relativePath).normalize();
        if (!bossPath.startsWith(projectRoot)) {
            throw new GameManifestValidationException("boss definition escapes project root: " + relativePath);
        }
        try (Reader reader = Files.newBufferedReader(bossPath)) {
            return bossLoader.load(reader);
        } catch (IOException exception) {
            throw new GameManifestValidationException("failed to read boss definition: " + relativePath, exception);
        }
    }
}
