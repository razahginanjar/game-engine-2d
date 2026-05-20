@echo off
setlocal

set "REPO_ROOT=%~dp0.."
pushd "%REPO_ROOT%" || exit /b 1

call mvn -pl tools-validation -am install "-DskipTests" || exit /b 1
call mvn -f tools-validation/pom.xml org.codehaus.mojo:exec-maven-plugin:3.2.0:java "-Dexec.args=--project-root \"%CD%\" --manifest sample-metroidvania/src/main/resources/game.manifest.json"
set "EXIT_CODE=%ERRORLEVEL%"

popd
exit /b %EXIT_CODE%
