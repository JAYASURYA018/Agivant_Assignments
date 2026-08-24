@echo off
echo ===================================================
echo   Running BookBasket Test Suite (64 JUnit 5 Tests)
echo ===================================================
call "%~dp0mvn.cmd" test
pause
