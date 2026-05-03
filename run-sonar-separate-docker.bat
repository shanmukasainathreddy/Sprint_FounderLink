@echo off
setlocal

if "%~1"=="" (
  echo Usage: run-sonar-separate-docker.bat ^<SONAR_HOST_URL^> ^<SONAR_TOKEN^> [PROJECT_KEY_PREFIX]
  exit /b 1
)

if "%~2"=="" (
  echo Usage: run-sonar-separate-docker.bat ^<SONAR_HOST_URL^> ^<SONAR_TOKEN^> [PROJECT_KEY_PREFIX]
  exit /b 1
)

set SONAR_HOST_URL=%~1
set SONAR_TOKEN=%~2
set PROJECT_KEY_PREFIX=%~3

if "%PROJECT_KEY_PREFIX%"=="" set PROJECT_KEY_PREFIX=FounderLink

call :scan_java_service api-gateway "API Gateway" || exit /b 1
call :scan_java_service auth-service "Auth Service" || exit /b 1
call :scan_java_service config-server "Config Server" || exit /b 1
call :scan_java_service eureka-server "Eureka Server" || exit /b 1
call :scan_java_service investment-service "Investment Service" || exit /b 1
call :scan_java_service messaging-service "Messaging Service" || exit /b 1
call :scan_java_service notification-service "Notification Service" || exit /b 1
call :scan_java_service startup-service "Startup Service" || exit /b 1
call :scan_java_service team-service "Team Service" || exit /b 1
call :scan_java_service user-service "User Service" || exit /b 1
call :scan_frontend || exit /b 1

echo Separate Docker SonarQube scans completed.
exit /b 0

:scan_java_service
set SERVICE_DIR=%~1
set SERVICE_NAME=%~2

echo Scanning %SERVICE_NAME% with Docker...
docker run --rm ^
  -e "SONAR_HOST_URL=%SONAR_HOST_URL%" ^
  -e "SONAR_TOKEN=%SONAR_TOKEN%" ^
  -v "%cd%:/usr/src" ^
  sonarsource/sonar-scanner-cli ^
  "-Dsonar.projectBaseDir=/usr/src/%SERVICE_DIR%" ^
  "-Dsonar.projectKey=%PROJECT_KEY_PREFIX%-%SERVICE_DIR%" ^
  "-Dsonar.projectName=%PROJECT_KEY_PREFIX% %SERVICE_NAME%" ^
  -Dsonar.sourceEncoding=UTF-8 ^
  -Dsonar.java.source=17 ^
  -Dsonar.sources=src/main ^
  -Dsonar.tests=src/test ^
  -Dsonar.java.binaries=target/classes ^
  -Dsonar.java.test.binaries=target/test-classes ^
  -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml ^
  -Dsonar.exclusions=**/target/**,**/logs/**,**/*.gz,**/.idea/**,**/.metadata/**,**/mvnw,**/mvnw.cmd,**/Dockerfile ^
  -Dsonar.coverage.exclusions=**/*Application.java,**/*Config.java,**/*Dto.java,**/*DTO.java,**/*Request.java,**/*Response.java,**/*Entity.java,**/*Repository.java,**/dto/**,**/entity/**,**/repository/**,**/config/**,**/security/**,**/client/**,**/producer/**,**/exception/**,**/*StatusController.java ^
  -Dsonar.qualitygate.wait=true
exit /b %ERRORLEVEL%

:scan_frontend
echo Scanning Frontend with Docker...
docker run --rm ^
  -e "SONAR_HOST_URL=%SONAR_HOST_URL%" ^
  -e "SONAR_TOKEN=%SONAR_TOKEN%" ^
  -v "%cd%:/usr/src" ^
  sonarsource/sonar-scanner-cli ^
  "-Dsonar.projectBaseDir=/usr/src/sprint frontend/founderlink" ^
  "-Dsonar.projectKey=%PROJECT_KEY_PREFIX%-frontend" ^
  "-Dsonar.projectName=%PROJECT_KEY_PREFIX% Frontend" ^
  -Dsonar.sourceEncoding=UTF-8 ^
  -Dsonar.sources=src/app,src/main.ts,src/styles.css ^
  -Dsonar.tests=src/app ^
  -Dsonar.test.inclusions=**/*.spec.ts ^
  -Dsonar.javascript.lcov.reportPaths=coverage/lcov.info,coverage/founderlink/lcov.info ^
  -Dsonar.exclusions=**/node_modules/**,**/dist/**,**/coverage/**,src/**/*.spec.ts ^
  -Dsonar.coverage.exclusions=**/main.ts,**/app.config.ts,**/app.routes.ts,**/*.spec.ts ^
  -Dsonar.qualitygate.wait=true
exit /b %ERRORLEVEL%
