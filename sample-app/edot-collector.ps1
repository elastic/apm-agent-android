$BaseDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$EdotBuildDir = Join-Path $BaseDir "edot-collector\build"

& "$BaseDir\gradlew.bat" ":edot-collector:prepareEdotCollector"

& "$EdotBuildDir\bin\otelcol.ps1" --config "$EdotBuildDir\configuration\edot-configuration.yml"
