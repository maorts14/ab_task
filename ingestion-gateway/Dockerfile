FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY order-core/pom.xml order-core/pom.xml
COPY ingestion-gateway/pom.xml ingestion-gateway/pom.xml
COPY normalizer-worker/pom.xml normalizer-worker/pom.xml
COPY mock-destination/pom.xml mock-destination/pom.xml
RUN mvn -pl ingestion-gateway -am dependency:go-offline -q
COPY order-core/src order-core/src
COPY ingestion-gateway/src ingestion-gateway/src
RUN mvn -pl ingestion-gateway -am package -DskipTests -q

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/ingestion-gateway/target/ingestion-gateway.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
