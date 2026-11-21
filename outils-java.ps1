function Trouver-JavaBin([bool]$Compilation) {
    $outil = if ($Compilation) { 'javac.exe' } else { 'java.exe' }
    $commande = Get-Command $outil -ErrorAction SilentlyContinue
    if ($commande) { return (Split-Path $commande.Source) }
    if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME "bin/$outil"))) {
        return (Join-Path $env:JAVA_HOME 'bin')
    }
    $dossierJdks = Join-Path $env:USERPROFILE '.jdks'
    if (Test-Path $dossierJdks) {
        foreach ($jdk in (Get-ChildItem -LiteralPath $dossierJdks -Directory | Sort-Object Name -Descending)) {
            $bin = Join-Path $jdk.FullName 'bin'
            if (Test-Path (Join-Path $bin $outil)) { return $bin }
        }
    }
    throw "Java introuvable. Installer un JDK (compilation) ou Java (execution), puis configurer JAVA_HOME ou PATH."
}
