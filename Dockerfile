# Build stage
FROM gradle:jdk26 AS builder

WORKDIR /app

# Copy files needed for dependency resolution first
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle gradle

# Download dependencies (helps Docker layer caching)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY src src

# Build the executable jar
RUN ./gradlew bootJar

# Runtime stage
FROM eclipse-temurin:26-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
