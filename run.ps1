Write-Host "==============================================================" -ForegroundColor Yellow
Write-Host "[NU-LSF] Starting National University Lost & Found System..." -ForegroundColor Cyan
Write-Host "==============================================================" -ForegroundColor Yellow

if (Test-Path "C:\Program Files\Eclipse Adoptium\jdk-25.0.5.8-hotspot") {
    $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-25.0.5.8-hotspot"
    $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
}

.\mvnw.cmd spring-boot:run

