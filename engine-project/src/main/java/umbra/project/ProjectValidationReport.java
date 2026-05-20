package umbra.project;

import java.util.List;
import java.util.Optional;

public record ProjectValidationReport(
        Optional<GameManifest> manifest,
        List<ProjectValidationIssue> issues
) {
    public ProjectValidationReport {
        manifest = manifest == null ? Optional.empty() : manifest;
        issues = issues == null ? List.of() : List.copyOf(issues);
    }

    public boolean valid() {
        return errors().isEmpty();
    }

    public List<ProjectValidationIssue> errors() {
        return issues.stream()
                .filter(issue -> issue.severity() == ProjectValidationSeverity.ERROR)
                .toList();
    }

    public List<ProjectValidationIssue> warnings() {
        return issues.stream()
                .filter(issue -> issue.severity() == ProjectValidationSeverity.WARNING)
                .toList();
    }
}
