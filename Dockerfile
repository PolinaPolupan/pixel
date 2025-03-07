FROM eclipse-temurin:17-jdk

WORKDIR /app
COPY build/libs/*.jar app.jar
RUN apt update
RUN apt install libopencv-dev -y
RUN mkdir -p /app/native-libs
COPY native/build/*.so /app/native-libs/
ENV LD_LIBRARY_PATH="/app/native-libs:${LD_LIBRARY_PATH}"

EXPOSE 8080

CMD ["java", "-Djava.library.path=/app/native-libs", "-jar", "app.jar"]