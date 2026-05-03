@echo off
setlocal

if "%~1"=="" (
  echo Usage: run-sonar.bat ^<SONAR_HOST_URL^> ^<SONAR_TOKEN^> [PROJECT_KEY]
  exit /b 1
)

if "%~2"=="" (
  echo Usage: run-sonar.bat ^<SONAR_HOST_URL^> ^<SONAR_TOKEN^> [PROJECT_KEY]
  exit /b 1
)

set SONAR_HOST_URL=%~1
set SONAR_TOKEN=%~2
set SONAR_PROJECT_KEY=%~3

if "%SONAR_PROJECT_KEY%"=="" set SONAR_PROJECT_KEY=FounderLink

sonar-scanner -Dsonar.host.url=%SONAR_HOST_URL% -Dsonar.token=%SONAR_TOKEN% -Dsonar.projectKey=%SONAR_PROJECT_KEY%
