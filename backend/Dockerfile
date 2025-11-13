# ============================
# Stage 1: Build the application
# ============================
FROM eclipse-temurin:21-jdk AS builder

# Install CA certificates to fix TLS errors (bad_record_mac)
USER root
RUN apt-get update && apt-get install -y ca-certificates && update-ca-certificates

# Set the working directory
WORKDIR /app

# Copy Gradle wrapper and config files
COPY gradlew .
 COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .

# Prepare wrapper permissions
RUN chmod +x gradlew

# Force fresh dependency download (prevents corrupted cache)
RUN ./gradlew --no-daemon --refresh-dependencies dependencies

# Copy the full source code
COPY src ./src

# Build fat jar only (bootJar)
RUN ./gradlew clean bootJar --no-daemon


# ============================
# Stage 2: Create the final runtime image
# ============================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy final jar
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]