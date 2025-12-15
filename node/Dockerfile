FROM python:3.12-slim

WORKDIR /app

COPY . .

RUN pip install -r requirements.txt
RUN pip install -e .

ENV PYTHONPATH=/app

CMD ["python", "-m", "uvicorn", "pixel.server.main:app", "--host", "0.0.0.0", "--port", "8000"]