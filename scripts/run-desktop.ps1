$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
Push-Location $repoRoot
try {
    mvn -pl desktop -am install "-DskipTests"
    mvn -f desktop/pom.xml org.codehaus.mojo:exec-maven-plugin:3.2.0:java
} finally {
    Pop-Location
}
