package umbra.project;

public record ProjectValidationIssue(
        ProjectValidationSeverity severity,
        String code,
        String message
) {
    public ProjectValidationIssue {
        if (severity == null) {
            throw new GameManifestValidationException("validation issue severity must not be null");
        }
        requireText("validation issue code", code);
        requireText("validation issue message", message);
    }

    private static void requireText(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new GameManifestValidationException(field + " must not be blank");
        }
    }
}
