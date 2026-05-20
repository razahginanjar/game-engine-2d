package umbra.project;

import umbra.animation.AnimationClipDefinition;
import umbra.animation.AnimationMetadataLoader;
import umbra.animation.AnimationSetDefinition;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
        validateCreatureDefinitions(absoluteRoot, manifest, issues);
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

    private static ProjectValidationIssue error(String code, String message) {
        return new ProjectValidationIssue(ProjectValidationSeverity.ERROR, code, message);
    }

    private static ProjectValidationIssue warning(String code, String message) {
        return new ProjectValidationIssue(ProjectValidationSeverity.WARNING, code, message);
    }
}
