$ErrorActionPreference = 'Stop'
. "$PSScriptRoot\use-tools.ps1"
$projectRoot = Split-Path $PSScriptRoot -Parent
Push-Location (Join-Path $projectRoot 'backend')
try { & mvn.cmd spring-boot:run } finally { Pop-Location }
