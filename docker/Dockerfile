# Stage 1: Build the application using a Gradle image with JDK 21.
FROM gradle:8.14.3-jdk21-corretto AS builder
WORKDIR /home/gradle/project

# Copy Gradle files from the correct locations.
COPY gradlew gradlew.bat ./
COPY gradle/wrapper/ gradle/wrapper/
COPY gradle/local.versions.toml gradle/
COPY settings.gradle.kts ./
COPY build.gradle.kts ./

# Make gradlew executable.
RUN chmod +x gradlew

# Download dependencies (this layer will be cached unless build files change).
RUN ./gradlew --no-daemon dependencies

# Copy the rest of the source code.
COPY . .

# Build the api subproject.
# Exclude tests to speed up the build.
RUN ./gradlew --no-daemon :api:clean :api:assemble

# Stage 2: Package the application into a runtime image using temurin JRE 21.
FROM eclipse-temurin:21.0.8_9-jre-jammy
WORKDIR /app

# Copy the generated jar from the builder stage.
COPY --from=builder /home/gradle/project/apps/api/build/libs/api-fat.jar app.jar

# Create the directory structure for the resources
RUN mkdir -p src/main/resources/webroot

# Copy the openapi.yaml file from the builder stage
COPY --from=builder /home/gradle/project/apps/api/src/main/resources/openapi.yaml src/main/resources/openapi.yaml

# Copy the webroot directory and its contents from the builder stage
COPY --from=builder /home/gradle/project/apps/api/src/main/resources/webroot/swagger-ui.html src/main/resources/webroot/swagger-ui.html

# Run the application.
CMD ["java", "-jar", "app.jar"]
