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

ENV VIRTUAL_ENV=/app/venv
ENV PATH="$VIRTUAL_ENV/bin:$PATH"
ENV PYTHONPATH="$VIRTUAL_ENV/lib/python3.12/site-packages"

RUN pip install --upgrade pip && \
    pip install poetry

COPY python/pyproject.toml python/poetry.lock* /app/python/

WORKDIR /app/python
RUN poetry config virtualenvs.in-project true && \
    poetry install --no-root

COPY python/ /app/python/

WORKDIR /app

COPY node-config/ /app/node-config/

CMD ["java", "-Djava.library.path=/app/native-libs", "-jar", "app.jar"]