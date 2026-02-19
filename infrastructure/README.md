# Infrastructure

Docker Compose configuration for the NetHelt project.

## Services

| Service | Port | Description |
|---------|------|-------------|
| nh-postgres | 5432 | PostgreSQL database |
| nh-web-api | 8080 | Spring Boot REST API |

## Environment Setup

1. Copy the example environment file:
   ```bash
   cp .env.example .env
   ```

2. Edit `.env` and set values:
   ```bash
   nano .env
   ```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| POSTGRES_USER | Database user | postgres |
| POSTGRES_PASSWORD | Database password | postgres |
| POSTGRES_DB | Database name | nh-db |

## Running

From the project root directory:

```bash
# Start all services
make up

# Stop all services
make down

# View logs
make logs

# Rebuild images
make build
```

Or using docker-compose directly fro the root directory:

```bash
docker-compose -f infrastructure/docker-compose.yml up -d
docker-compose -f infrastructure/docker-compose.yml down
```

## Volumes

- `nh-postgres-data` - PostgreSQL data persistence
