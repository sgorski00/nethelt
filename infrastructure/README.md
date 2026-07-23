# NetHelt Infrastructure

Docker Compose configuration for the NetHelt project.

## Services

| Service | Port | Description |
|---------|------|-------------|
| `nh-postgres` | 5432 | PostgreSQL database |
| `nh-web-api` | 8080 | Spring Boot REST API |
| `nh-frontend` | 80 | Angular web application |
| `nh-ai-model` | - | AI module for network anomaly detection |

## Environment Setup

1. Create a local environment file:
```bash
cp .env.example .env
```

2. Edit `.env` and provide required values:
```bash
nano .env
```

### Environment Variables

#### Database

| Variable | Description | Default |
|----------|-------------|---------|
| `POSTGRES_USER` | Database user | `postgres` |
| `POSTGRES_PASSWORD` | Database password | `postgres` |
| `POSTGRES_DB` | Database name | `nh-db` |

#### Security

| Variable | Description |
|----------|-------------|
| `JWT_SECRET_KEY` | Secret key used for signing JWT tokens |
| `JWT_EXPIRATION` | Access token expiration time |
| `REFRESH_TOKEN_EXPIRATION` | Refresh token expiration time |

#### OAuth2

| Variable | Description |
|----------|-------------|
| `GOOGLE_CLIENT_ID` | Google OAuth2 client ID |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 client secret |
| `GITHUB_CLIENT_ID` | GitHub OAuth2 client ID |
| `GITHUB_CLIENT_SECRET` | GitHub OAuth2 client secret |

#### Frontend

| Variable | Description |
|----------|-------------|
| `FRONTEND_URL` | Frontend application URL |
| `FRONTEND_OAUTH2_SUCCESS_URL` | OAuth2 success redirect URL |
| `FRONTEND_OAUTH2_FAILURE_URL` | OAuth2 failure redirect URL |
| `ALLOWED_CORS_ORIGINS` | Allowed frontend origins |

#### Mail

| Variable | Description |
|----------|-------------|
| `SPRING_MAIL_HOST` | SMTP server host |
| `SPRING_MAIL_PORT` | SMTP server port |
| `SPRING_MAIL_USERNAME` | SMTP username |
| `SPRING_MAIL_PASSWORD` | SMTP password |

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

Or using docker-compose directly:

Run services:
```bash
docker-compose -f infrastructure/docker-compose.yml up -d
```

Stop services:
```bash
docker-compose -f infrastructure/docker-compose.yml down
```

## Access

After starting the stack:

| Service | URL |
|---------|-----|
| Frontend | http://localhost |
| Web API | http://localhost:8080 |

## Volumes

- `nh-postgres-data` - PostgreSQL data persistence

## Network

All services communicate through the Docker bridge network: `nh-network`

Internal service communication uses container names:

```
nh-postgres:5432
nh-web-api:8080
```