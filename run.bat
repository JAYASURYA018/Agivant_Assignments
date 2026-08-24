@echo off
echo =======================================================
echo   Starting BookBasket Smart Library Platform...
echo =======================================================

:: Free port 8080 if lingering
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do (
    taskkill /F /PID %%a >nul 2>&1
)

call "%~dp0mvn.cmd" spring-boot:run
pause
