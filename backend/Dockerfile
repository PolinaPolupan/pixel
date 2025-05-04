FROM eclipse-temurin:17-jdk

RUN apt-get update && \
    apt-get install -y \
        libopencv-dev \
        python3 \
        python3-pip \
        python3-dev \
        python3.12-venv && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
RUN mkdir -p /app/native-libs

ENV LD_LIBRARY_PATH="/app/native-libs:${LD_LIBRARY_PATH}"
EXPOSE 8080

COPY native/build/*.so /app/native-libs/
COPY build/libs/*.jar app.jar

RUN python3 -m venv /app/venv

COPY python/requirements.txt /app/python/
RUN /app/venv/bin/pip install --no-cache-dir -r /app/python/requirements.txt

COPY python/ /app/python/

CMD ["java", "-Djava.library.path=/app/native-libs", "-jar", "app.jar"]