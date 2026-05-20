$ErrorActionPreference = "Stop"

$RepoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")

Push-Location $RepoRoot
try {
    mvn -pl tools-validation -am install "-DskipTests"
    mvn -f tools-validation/pom.xml org.codehaus.mojo:exec-maven-plugin:3.2.0:java "-Dexec.args=--project-root `"$RepoRoot`" --manifest sample-metroidvania/src/main/resources/game.manifest.json"
} finally {
    Pop-Location
}
