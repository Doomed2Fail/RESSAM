$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'outils-java.ps1')
$javaBinPaint = Trouver-JavaBin $true
$classesPaint = Join-Path $PSScriptRoot 'build/classes'
$distPaint = Join-Path $PSScriptRoot 'dist'
New-Item -ItemType Directory -Force -Path $classesPaint, $distPaint | Out-Null
$sourcesPaint = @(Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot 'src') -Filter '*.java' | ForEach-Object { $_.FullName })
$versionPaint = (& (Join-Path $javaBinPaint 'javac.exe') -version 2>&1 | Out-String)
$compatibilitePaint = if ($versionPaint -match 'javac 1\.8\.') { @('-source', '8', '-target', '8') } else { @('--release', '8') }
& (Join-Path $javaBinPaint 'javac.exe') -encoding UTF-8 -Xlint:-options @compatibilitePaint -d $classesPaint @sourcesPaint
if ($LASTEXITCODE -ne 0) { throw 'Compilation Java echouee.' }
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'resources/messages') -Destination $classesPaint -Recurse -Force
& (Join-Path $javaBinPaint 'jar.exe') cfe (Join-Path $distPaint 'Paint-ruban-compact.jar') Paint -C $classesPaint .
if ($LASTEXITCODE -ne 0) { throw 'Creation du JAR echouee.' }
Write-Host "Compilation terminee : $distPaint/Paint-ruban-compact.jar"
