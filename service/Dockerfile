FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy only the module sources needed to build the service jar
COPY pom.xml ./
COPY service ./service
COPY gatling ./gatling
COPY infrastructure ./infrastructure

RUN mvn -pl service -am -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /workspace/service/target/service-0.1.0-SNAPSHOT.jar /app/service.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/service.jar"]
