$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$localJava = Get-ChildItem -LiteralPath (Join-Path $projectRoot '.tools') -Directory -Filter 'jdk-21*' -ErrorAction SilentlyContinue | Select-Object -First 1
if ($localJava) { $env:JAVA_HOME = $localJava.FullName; $env:Path = "$env:JAVA_HOME\bin;$env:Path" }
$localNode = Join-Path $projectRoot '.tools\node-v22.16.0-win-x64'
$localMaven = Join-Path $projectRoot '.tools\apache-maven-3.9.9\bin'
if (Test-Path -LiteralPath $localNode) { $env:Path = "$localNode;$env:Path" }
if (Test-Path -LiteralPath $localMaven) { $env:Path = "$localMaven;$env:Path" }
