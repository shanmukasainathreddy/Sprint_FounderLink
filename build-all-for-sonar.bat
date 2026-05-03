@echo off
setlocal

call mvn -f api-gateway\pom.xml clean verify || exit /b 1
call mvn -f auth-service\pom.xml clean verify || exit /b 1
call mvn -f config-server\pom.xml clean verify || exit /b 1
call mvn -f eureka-server\pom.xml clean verify || exit /b 1
call mvn -f investment-service\pom.xml clean verify || exit /b 1
call mvn -f messaging-service\pom.xml clean verify || exit /b 1
call mvn -f notification-service\pom.xml clean verify || exit /b 1
call mvn -f startup-service\pom.xml clean verify || exit /b 1
call mvn -f team-service\pom.xml clean verify || exit /b 1
call mvn -f user-service\pom.xml clean verify || exit /b 1

pushd "sprint frontend\founderlink"
call npm ci || exit /b 1
call npm run test:ci || exit /b 1
call npm run build || exit /b 1
popd

echo Build completed for Sonar analysis.
