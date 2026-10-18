$ErrorActionPreference = 'Stop'
. "$PSScriptRoot\use-tools.ps1"
$projectRoot = Split-Path $PSScriptRoot -Parent
Push-Location (Join-Path $projectRoot 'frontend')
try { & npm.cmd run dev } finally { Pop-Location }
