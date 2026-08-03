# Refresh Path environment variable to inherit new installations (like Docker)
$env:Path = [Environment]::GetEnvironmentVariable("Path", "Machine") + ";" + [Environment]::GetEnvironmentVariable("Path", "User")

$MavenVersion = "3.9.6"
$MavenDir = Join-Path $PSScriptRoot ".maven"
$MavenHome = Join-Path $MavenDir "apache-maven-$MavenVersion"
$MvnCmd = Join-Path $MavenHome "bin\mvn.cmd"

if (-not (Test-Path $MvnCmd)) {
    Write-Host "Local Maven not found. Bootstrapping Maven $MavenVersion..."
    if (-not (Test-Path $MavenDir)) {
        New-Item -ItemType Directory -Path $MavenDir | Out-Null
    }
    
    $ZipPath = Join-Path $MavenDir "maven.zip"
    $DownloadUrl = "https://archive.apache.org/dist/maven/maven-3/$MavenVersion/binaries/apache-maven-$MavenVersion-bin.zip"
    
    Write-Host "Downloading Maven from $DownloadUrl..."
    Invoke-WebRequest -Uri $DownloadUrl -OutFile $ZipPath
    
    Write-Host "Extracting Maven to $MavenDir..."
    Expand-Archive -Path $ZipPath -DestinationPath $MavenDir -Force
    
    Write-Host "Removing temporary zip..."
    Remove-Item $ZipPath
    
    Write-Host "Maven bootstrapped successfully at $MavenHome"
}

# Run the command with arguments passed to the script
if ($args.Count -eq 0) {
    & $MvnCmd clean test-compile
} else {
    & $MvnCmd $args
}
