# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the executable jar skipping test suites (already verified in CI/CD)
RUN mvn clean package -DskipTests

# Stage 2: Minimal runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl for health checking in container
RUN apk add --no-cache curl

# Create dedicated non-root system group and user
RUN addgroup -S ingestion && adduser -S ingestion -G ingestion

# Copy the built jar from the builder stage
COPY --from=builder /build/target/generic-data-ingestion-platform-1.0.0-SNAPSHOT.jar app.jar

# Enforce non-root ownership of app folder contents
RUN chown -R ingestion:ingestion /app

# Run as non-root user
USER ingestion

# Expose production port
EXPOSE 8080

# Configure default G1 Garbage Collector and RAM allocation limits
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75 -Dspring.profiles.active=prod"

# Health check using liveness probe actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health/liveness || exit 1

# Execute runtime jar
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
