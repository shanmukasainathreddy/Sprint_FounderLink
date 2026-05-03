# SonarQube setup

This project includes a root `sonar-project.properties` file so you can scan all backend services and the Angular frontend from the repository root.
Coverage is reported from JaCoCo XML files for Java and LCOV for Angular.

There are two scan modes:

- Aggregate scan: shows everything inside one SonarQube project named `FounderLink`.
- Separate scan: creates one SonarQube project per service/frontend so each project has its own coverage.

## 0. Start Docker services

Run this from the project root:

```bat
docker compose --profile sonar up -d sonarqube-db sonarqube
```

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

## 1. Build tests and coverage first

Run this command from the project root:

```bat
build-all-for-sonar.bat
```

It runs Maven `clean verify` for every backend service, then runs Angular tests with coverage and builds the frontend.
If you prefer the expanded backend commands, they are:

```bat
call mvn -f api-gateway\pom.xml clean verify
call mvn -f auth-service\pom.xml clean verify
call mvn -f config-server\pom.xml clean verify
call mvn -f eureka-server\pom.xml clean verify
call mvn -f investment-service\pom.xml clean verify
call mvn -f messaging-service\pom.xml clean verify
call mvn -f notification-service\pom.xml clean verify
call mvn -f startup-service\pom.xml clean verify
call mvn -f team-service\pom.xml clean verify
call mvn -f user-service\pom.xml clean verify
```

Frontend coverage is generated with:

```bat
cd "sprint frontend\founderlink"
npm ci
npm run test:ci
npm run build
cd ..\..
```

You can run only the services you want to scan, but Sonar works best when all matching `target/classes`, JaCoCo XML, and LCOV files already exist.

## 2. Run SonarQube scan

### Option A: separate projects and separate coverage

Use this if you want SonarQube to show each service separately instead of only one `FounderLink` project:

```bat
run-sonar-separate.bat http://localhost:9000 YOUR_SONAR_TOKEN FounderLink
```

This creates projects like:

```text
FounderLink-api-gateway
FounderLink-auth-service
FounderLink-config-server
FounderLink-eureka-server
FounderLink-investment-service
FounderLink-messaging-service
FounderLink-notification-service
FounderLink-startup-service
FounderLink-team-service
FounderLink-user-service
FounderLink-frontend
```

The separate scan excludes boilerplate packages such as `dto`, `entity`, `repository`, `config`, `security`, `client`, `producer`, and exception handlers from coverage. SonarQube will still analyze those files for issues, but the coverage gate focuses on business logic and tested controllers.

If you do not want to install `sonar-scanner`, use Docker for the scanner:

```bat
run-sonar-separate-docker.bat http://host.docker.internal:9000 YOUR_SONAR_TOKEN FounderLink
```

Use `host.docker.internal` when the scanner runs inside Docker and SonarQube is running on your Windows host through Docker Desktop.

### Option B: one aggregate FounderLink project

After SonarQube is running and you have created a token, install `sonar-scanner` locally, then from the project root run:

```bat
sonar-scanner -Dsonar.host.url=http://localhost:9000 -Dsonar.token=YOUR_SONAR_TOKEN
```

Or use the helper script added to this repo:

```bat
run-sonar.bat http://localhost:9000 YOUR_SONAR_TOKEN FounderLink
```

If you do not want to install `sonar-scanner`, use Docker for the scanner too:

```bat
run-sonar-docker.bat http://host.docker.internal:9000 YOUR_SONAR_TOKEN FounderLink
```

Use `host.docker.internal` when the scanner runs inside Docker and SonarQube is running on your Windows host through Docker Desktop.

If your SonarQube project key is different, override it:

```bat
sonar-scanner -Dsonar.projectKey=your-project-key -Dsonar.host.url=http://localhost:9000 -Dsonar.token=YOUR_SONAR_TOKEN
```

## 3. Optional: use SonarLint in IntelliJ or VS Code

SonarLint is an IDE plugin, not a repo dependency. Install the SonarLint extension in your editor and open this folder. If you connect SonarLint to your SonarQube server, it will reuse the same project rules.

## 4. Set an 80% quality gate

In SonarQube, open:

```text
Quality Gates -> Create
```

Recommended conditions:

```text
Coverage on New Code >= 80%
Duplicated Lines on New Code <= 3%
Reliability Rating on New Code = A
Security Rating on New Code = A
Maintainability Rating on New Code = A
```

Then assign the gate to the `FounderLink` project, or to each separate project if you are using `run-sonar-separate.bat`. The repository can generate the reports, but SonarQube decides whether each project passes the 80% gate.

GitHub Actions also runs Maven `verify` and Angular `test:ci`, then uploads the JaCoCo and LCOV reports as artifacts. A red cross in GitHub means at least one build, test, coverage, or Docker Compose step failed.

## Quick flow

```bat
docker compose --profile sonar up -d sonarqube-db sonarqube
build-all-for-sonar.bat
run-sonar-separate.bat http://localhost:9000 YOUR_SONAR_TOKEN FounderLink
```

Docker-only scanner flow:

```bat
docker compose --profile sonar up -d sonarqube-db sonarqube
build-all-for-sonar.bat
run-sonar-separate-docker.bat http://host.docker.internal:9000 YOUR_SONAR_TOKEN FounderLink
```
