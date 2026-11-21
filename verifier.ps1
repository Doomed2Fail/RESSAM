$ErrorActionPreference = 'Stop'
& (Join-Path $PSScriptRoot 'compiler.ps1')
. (Join-Path $PSScriptRoot 'outils-java.ps1')
$javaBinPaint = Trouver-JavaBin $true
$testsPaint = Join-Path $PSScriptRoot 'build/tests'
$classesPaint = Join-Path $PSScriptRoot 'build/classes'
New-Item -ItemType Directory -Force -Path $testsPaint | Out-Null
$sourcesTestPaint = @(Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot 'tests') -Filter '*.java' | ForEach-Object { $_.FullName })
$versionPaint = (& (Join-Path $javaBinPaint 'javac.exe') -version 2>&1 | Out-String)
$compatibilitePaint = if ($versionPaint -match 'javac 1\.8\.') { @('-source', '8', '-target', '8') } else { @('--release', '8') }
& (Join-Path $javaBinPaint 'javac.exe') -encoding UTF-8 -Xlint:-options @compatibilitePaint -cp $classesPaint -d $testsPaint @sourcesTestPaint
if ($LASTEXITCODE -ne 0) { throw 'Compilation des verifications echouee.' }
$cheminClassesPaint = (Join-Path $PSScriptRoot 'dist/Paint-ruban-compact.jar') + ';' + $testsPaint
& (Join-Path $javaBinPaint 'java.exe') '-Djava.awt.headless=true' -cp $cheminClassesPaint VerificationDessin (Join-Path $PSScriptRoot 'verification/tests')
if ($LASTEXITCODE -ne 0) { throw 'Verification du dessin echouee.' }
& (Join-Path $javaBinPaint 'java.exe') '-Djava.awt.headless=false' -cp $cheminClassesPaint VerificationInterface (Join-Path $PSScriptRoot 'verification')
if ($LASTEXITCODE -ne 0) { throw "Verification de l'interface echouee." }
