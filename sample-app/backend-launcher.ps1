$BaseDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$BackendBuildDir = Join-Path $BaseDir "backend\build"

& "$BaseDir\gradlew.bat" ":backend:bootJar"

$env:OTEL_SERVICE_NAME = "weather-backend"

& "java" "-jar" "$BackendBuildDir\libs\backend.jar"
