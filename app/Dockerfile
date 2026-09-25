# Das Jar baut die Pipeline nach bestandenen Tests; das Image enthält nur die Laufzeit.
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S shop && adduser -S shop -G shop
WORKDIR /app
COPY target/shop-*.jar app.jar
USER shop
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health/readiness || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
