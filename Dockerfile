FROM eclipse-temurin:17-jdk

RUN apt-get update && \
    apt-get install -y libopencv-dev && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
RUN mkdir -p /app/native-libs

ENV LD_LIBRARY_PATH="/app/native-libs:${LD_LIBRARY_PATH}"
EXPOSE 8080

COPY native/build/*.so /app/native-libs/
COPY build/libs/*.jar app.jar

CMD ["java", "-Djava.library.path=/app/native-libs", "-jar", "app.jar"]