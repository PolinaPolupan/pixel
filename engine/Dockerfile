FROM gradle:8.5-jdk17 AS build

WORKDIR /build

COPY . .

RUN gradle build -x test --no-daemon

FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY --from=build /build/build/libs/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
