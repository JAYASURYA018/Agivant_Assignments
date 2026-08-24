@echo off
set "MAVEN_PATH=C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.4\plugins\maven\lib\maven3\bin\mvn.cmd"

if exist "%MAVEN_PATH%" (
    call "%MAVEN_PATH%" %*
) else (
    mvn %*
)
