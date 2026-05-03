# SonarQube setup

This project now includes a root `sonar-project.properties` file so you can scan all services from the repository root.
The Docker Compose setup now also starts SonarQube automatically.

## 0. Start Docker services

Run this from the project root:

```bat
docker compose up -d
```

This now starts your application stack and SonarQube together.
SonarQube will be available at:

```text
http://localhost:9000
```

Default first-login credentials for a fresh SonarQube instance are usually:

```text
username: admin
password: admin
```

On first login, SonarQube will ask you to change the password and create a token.

## 1. Build the Java classes first

Run this command from the project root:

```bat
build-all-for-sonar.bat
```

It runs the same Maven wrappers for every service. If you prefer the expanded commands, they are:

```bat
call api-gateway\mvnw.cmd -f api-gateway\pom.xml clean verify
call auth-service\mvnw.cmd -f auth-service\pom.xml clean verify
call config-server\mvnw.cmd -f config-server\pom.xml clean verify
call eureka-server\mvnw.cmd -f eureka-server\pom.xml clean verify
call investment-service\mvnw.cmd -f investment-service\pom.xml clean verify
call messaging-service\mvnw.cmd -f messaging-service\pom.xml clean verify
call notification-service\mvnw.cmd -f notification-service\pom.xml clean verify
call startup-service\mvnw.cmd -f startup-service\pom.xml clean verify
call team-service\mvnw.cmd -f team-service\pom.xml clean verify
call user-service\mvnw.cmd -f user-service\pom.xml clean verify
```

You can also run only the services you want to scan, but Sonar works best when the matching `target/classes` folders already exist.

## 2. Run SonarQube scan

After SonarQube is running and you have created a token, install `sonar-scanner` locally, then from the project root run:

```bat
sonar-scanner -Dsonar.host.url=http://localhost:9000 -Dsonar.token=YOUR_SONAR_TOKEN
```

Or use the helper script added to this repo:

```bat
run-sonar.bat http://localhost:9000 YOUR_SONAR_TOKEN FounderLink
```

If your SonarQube project key is different, override it:

```bat
sonar-scanner -Dsonar.projectKey=your-project-key -Dsonar.host.url=http://localhost:9000 -Dsonar.token=YOUR_SONAR_TOKEN
```

## 3. Optional: use SonarLint in IntelliJ or VS Code

SonarLint is an IDE plugin, not a repo dependency. Install the SonarLint extension in your editor and open this folder. If you connect SonarLint to your SonarQube server, it will reuse the same project rules.

## Quick flow

```bat
docker compose up -d
build-all-for-sonar.bat
run-sonar.bat http://localhost:9000 YOUR_SONAR_TOKEN FounderLink
```
