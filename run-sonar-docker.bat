@echo off
setlocal

if "%~1"=="" (
  echo Usage: run-sonar-docker.bat ^<SONAR_HOST_URL^> ^<SONAR_TOKEN^> [PROJECT_KEY]
  exit /b 1
)

if "%~2"=="" (
  echo Usage: run-sonar-docker.bat ^<SONAR_HOST_URL^> ^<SONAR_TOKEN^> [PROJECT_KEY]
  exit /b 1
)

set SONAR_HOST_URL=%~1
set SONAR_TOKEN=%~2
set SONAR_PROJECT_KEY=%~3

if "%SONAR_PROJECT_KEY%"=="" set SONAR_PROJECT_KEY=FounderLink

docker run --rm ^
  -e SONAR_HOST_URL=%SONAR_HOST_URL% ^
  -e SONAR_TOKEN=%SONAR_TOKEN% ^
  -v "%cd%:/usr/src" ^
  sonarsource/sonar-scanner-cli ^
  -Dsonar.projectKey=%SONAR_PROJECT_KEY%
