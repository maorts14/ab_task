FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY order-core/pom.xml order-core/pom.xml
COPY ingestion-gateway/pom.xml ingestion-gateway/pom.xml
COPY normalizer-worker/pom.xml normalizer-worker/pom.xml
COPY mock-destination/pom.xml mock-destination/pom.xml
RUN mvn -pl normalizer-worker -am dependency:go-offline -q
COPY order-core/src order-core/src
COPY normalizer-worker/src normalizer-worker/src
RUN mvn -pl normalizer-worker -am package -DskipTests -q

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/normalizer-worker/target/normalizer-worker.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
