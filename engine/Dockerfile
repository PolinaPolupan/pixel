FROM gradle:8.5-jdk17 AS build

WORKDIR /build

COPY . .

# Use the Gradle wrapper if present (safer), otherwise fall back to image gradle.
# Build and then list likely output folders so CI shows where the JAR actually landed.
RUN if [ -x ./gradlew ]; then ./gradlew build -x test --no-daemon; else gradle build -x test --no-daemon; fi \
    && echo "=== listing root build dir ===" && ls -la /build || true \
    && echo "=== listing /build/libs ===" && ls -la /build/libs || true \
    && echo "=== listing /build/build/libs ===" && ls -la /build/build/libs || true \
    && echo "=== listing subproject build dirs ===" && find . -maxdepth 3 -type d -path "*/build/libs" -print -exec ls -la {} \; || true

FROM eclipse-temurin:17-jdk

WORKDIR /app

# Robust copy: pick any produced jar from the common build output path
COPY --from=build /build/**/build/libs/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
