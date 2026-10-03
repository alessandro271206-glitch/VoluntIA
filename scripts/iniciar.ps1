param([switch]$Pruebas)
$ErrorActionPreference = 'Stop'
$projectPath = Split-Path -Parent $PSScriptRoot
if (Test-Path -LiteralPath 'C:\Program Files\Java\jdk-17') { $env:JAVA_HOME = 'C:\Program Files\Java\jdk-17' }
Push-Location -LiteralPath $projectPath
try {
    $task = if ($Pruebas) { 'test' } else { 'spring-boot:run' }
    & (Join-Path $projectPath 'mvnw.cmd') $task
    if ($LASTEXITCODE -ne 0) { throw 'Maven no terminó correctamente. Revisa el mensaje anterior.' }
} finally { Pop-Location }
