$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'outils-java.ps1')
$javaBinPaint = Trouver-JavaBin $false
$jarPaint = Join-Path $PSScriptRoot 'dist/Paint-ruban-compact.jar'
if (-not (Test-Path $jarPaint)) { & (Join-Path $PSScriptRoot 'compiler.ps1') }
& (Join-Path $javaBinPaint 'java.exe') -jar $jarPaint
if ($LASTEXITCODE -ne 0) { throw "L'application a signale une erreur." }
