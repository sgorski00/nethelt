FROM python:3.12-slim AS builder

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
RUN curl -sSL https://install.python-poetry.org | python3 -

ENV PATH="/root/.local/bin:$PATH"

WORKDIR /app
COPY ./ai-model/pyproject.toml ./ai-model/poetry.lock ./

RUN poetry self add poetry-plugin-export
RUN poetry export --without-hashes -f requirements.txt -o requirements.txt

FROM python:3.12-slim

WORKDIR /app

COPY --from=builder /app/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY ./ai-model/src/ai_model/ .

CMD ["python", "-u", "main.py"]
