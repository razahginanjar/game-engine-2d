package umbra.project;

public record SavePolicy(
        boolean enabled,
        String path
) {
    public SavePolicy {
        if (enabled && (path == null || path.isBlank())) {
            throw new GameManifestValidationException("save.path must not be blank when saving is enabled");
        }
        if (path == null) {
            path = "";
        }
    }
}
