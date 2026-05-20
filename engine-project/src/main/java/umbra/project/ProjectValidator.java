package umbra.project;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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

    private static ProjectValidationIssue error(String code, String message) {
        return new ProjectValidationIssue(ProjectValidationSeverity.ERROR, code, message);
    }

    private static ProjectValidationIssue warning(String code, String message) {
        return new ProjectValidationIssue(ProjectValidationSeverity.WARNING, code, message);
    }
}
