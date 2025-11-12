# Stage 1: Build the application
# We use a Java 21 JDK image as requested
FROM eclipse-temurin:21-jdk AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the Gradle wrapper and build file
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .

# ✅ FIX: Force the JVM to use TLSv1.2 for network requests
ENV JAVA_OPTS="-Dhttps.protocols=TLSv1.2"

# Download dependencies
RUN ./gradlew dependencies

# Copy the rest of the source code
COPY src ./src

# Change 'build' to 'bootJar' to build only the executable fat jar
RUN ./gradlew bootJar --no-daemon

# Stage 2: Create the final, small image
FROM eclipse-temurin:21-jre

WORKDIR /app

# Explicitly copy the correct jar file (based on your build.gradle)
COPY --from=builder /app/build/libs/backend-0.0.1-SNAPSHOT.jar app.jar

# Expose the port that Spring Boot runs on
EXPOSE 8080

# This is the command that will run when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]