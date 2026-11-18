FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /src
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY platform-common platform-common
COPY order-service order-service
COPY inventory-service inventory-service
COPY payment-service payment-service
COPY api-gateway api-gateway
COPY platform-tests platform-tests
RUN chmod +x mvnw && ./mvnw -B -DskipTests -pl payment-service -am package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 app
COPY --from=build /src/payment-service/target/payment-service-exec.jar app.jar
USER app
EXPOSE 8083
HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=12 \
    CMD curl -fsS http://localhost:8083/actuator/health/readiness || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
