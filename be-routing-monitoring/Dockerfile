# Build the Spring Boot jar with JDK 21...
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
# Copy build files first so dependency resolution is cached across code changes.
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
# Ensure the wrapper is executable even when the repo was cloned on Windows,
# where the file's execute bit can be lost.
RUN chmod +x gradlew
RUN ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true
COPY src ./src
# Tests need Docker (Testcontainers), so we skip them in the image build.
RUN ./gradlew --no-daemon clean bootJar -x test

# ...then run it on a slim JRE.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
