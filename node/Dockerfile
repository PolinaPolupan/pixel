FROM python:3.12-slim

WORKDIR /app

COPY . .

ENV PIP_DEFAULT_TIMEOUT=200
ENV PIP_RETRIES=5
ENV PIP_NO_CACHE_DIR=1

RUN pip install -r requirements.txt
RUN pip install -e .
RUN apt-get update && apt-get install -y docker-cli

ENV PYTHONPATH=/app

CMD ["python", "-m", "uvicorn", "pixel.server.main:app", "--host", "0.0.0.0", "--port", "8000"]