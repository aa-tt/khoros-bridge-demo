FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY services services
ARG SERVICE=rest-service
RUN mvn -B -f services/pom.xml -pl ${SERVICE} -am package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
ARG SERVICE=rest-service
COPY --from=build /workspace/services/${SERVICE}/target/${SERVICE}-0.1.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
