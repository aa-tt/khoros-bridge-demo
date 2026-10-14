# Java services

This Maven reactor contains four separately buildable Spring Boot applications and a shared event helper. Deploy each app as its own ECS Fargate service and scale it independently. The shared image recipe is `Dockerfile`; build with `--build-arg SERVICE=rest-service` (also `sqs-worker`, `msk-worker`, or `reporting-service`).

## Build

Requires JDK 17+ and Maven 3.9+:

```powershell
mvn -f services/pom.xml clean verify
```

Build one image from the repository root:

```powershell
docker build -f services/Dockerfile --build-arg SERVICE=rest-service -t khoros-bridge/rest-service:local .
```

Every service README lists its environment variables and AWS behavior. AWS SDK clients use the ECS task role's credentials; do not configure static keys in task definitions.
