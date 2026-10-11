# ===== STAGE 1: Build =====
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copy Maven wrapper and pom.xml first (Docker layer caching)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy source and build (skip quality/security plugins - CI runs them)
COPY src src
RUN ./mvnw clean package -DskipTests -Dcheckstyle.skip=true -Dspotbugs.skip=true -Ddependency-check.skip=true -B

# ===== STAGE 2: Runtime =====
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port (Render sets PORT env var; Spring reads it via server.port)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]