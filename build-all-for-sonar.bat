@echo off
setlocal

call api-gateway\mvnw.cmd -f api-gateway\pom.xml clean verify || exit /b 1
call auth-service\mvnw.cmd -f auth-service\pom.xml clean verify || exit /b 1
call config-server\mvnw.cmd -f config-server\pom.xml clean verify || exit /b 1
call eureka-server\mvnw.cmd -f eureka-server\pom.xml clean verify || exit /b 1
call investment-service\mvnw.cmd -f investment-service\pom.xml clean verify || exit /b 1
call messaging-service\mvnw.cmd -f messaging-service\pom.xml clean verify || exit /b 1
call notification-service\mvnw.cmd -f notification-service\pom.xml clean verify || exit /b 1
call startup-service\mvnw.cmd -f startup-service\pom.xml clean verify || exit /b 1
call team-service\mvnw.cmd -f team-service\pom.xml clean verify || exit /b 1
call user-service\mvnw.cmd -f user-service\pom.xml clean verify || exit /b 1

echo Build completed for Sonar analysis.
