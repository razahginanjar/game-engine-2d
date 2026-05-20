package umbra.tools;

import umbra.project.ProjectValidationIssue;
import umbra.project.ProjectValidationReport;
import umbra.project.ProjectValidationSeverity;
import umbra.project.ProjectValidator;

import java.io.PrintStream;
import java.nio.file.Path;

public final class ProjectValidatorCli {
    private static final String DEFAULT_MANIFEST = "sample-metroidvania/src/main/resources/game.manifest.json";

    private ProjectValidatorCli() {
    }

    public static void main(String[] args) {
        int exitCode = run(args, System.out, System.err);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    static int run(String[] args, PrintStream out, PrintStream err) {
        CliOptions options = CliOptions.parse(args);
        if (options.help()) {
            printUsage(out);
            return 0;
        }
        if (options.error() != null) {
            err.println(options.error());
            printUsage(err);
            return 2;
        }

        ProjectValidationReport report = new ProjectValidator().validate(options.projectRoot(), options.manifestPath());
        report.manifest().ifPresent(manifest -> out.println("Project: " + manifest.title()));
        for (ProjectValidationIssue issue : report.issues()) {
            PrintStream stream = issue.severity() == ProjectValidationSeverity.ERROR ? err : out;
            stream.println(issue.severity() + " " + issue.code() + ": " + issue.message());
        }
        if (report.valid()) {
            out.println("Project validation passed with " + report.warnings().size() + " warning(s).");
            return 0;
        }
        err.println("Project validation failed with " + report.errors().size() + " error(s).");
        return 1;
    }

    private static void printUsage(PrintStream stream) {
        stream.println("Usage: validate-project [--project-root <path>] [--manifest <path>]");
        stream.println("Defaults: --project-root . --manifest " + DEFAULT_MANIFEST);
    }

    private record CliOptions(Path projectRoot, String manifestPath, boolean help, String error) {
        private static CliOptions parse(String[] args) {
            Path projectRoot = Path.of(".");
            String manifestPath = DEFAULT_MANIFEST;
            for (int index = 0; index < args.length; index++) {
                String arg = args[index];
                if ("--help".equals(arg) || "-h".equals(arg)) {
                    return new CliOptions(projectRoot, manifestPath, true, null);
                }
                if ("--project-root".equals(arg)) {
                    if (index + 1 >= args.length) {
                        return new CliOptions(projectRoot, manifestPath, false, "Missing value for --project-root");
                    }
                    projectRoot = Path.of(args[++index]);
                    continue;
                }
                if ("--manifest".equals(arg)) {
                    if (index + 1 >= args.length) {
                        return new CliOptions(projectRoot, manifestPath, false, "Missing value for --manifest");
                    }
                    manifestPath = args[++index];
                    continue;
                }
                return new CliOptions(projectRoot, manifestPath, false, "Unknown argument: " + arg);
            }
            return new CliOptions(projectRoot, manifestPath, false, null);
        }
    }
}
