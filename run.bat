@echo off
setlocal
echo ==============================================================
echo [NU-LSF] Starting National University Lost ^& Found System...
echo ==============================================================

if exist "C:\Program Files\Eclipse Adoptium\jdk-25.0.5.8-hotspot" (
    set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.5.8-hotspot"
    set "PATH=C:\Program Files\Eclipse Adoptium\jdk-25.0.5.8-hotspot\bin;%PATH%"
)

call .\mvnw.cmd spring-boot:run
pause

