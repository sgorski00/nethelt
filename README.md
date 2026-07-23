[![codecov](https://codecov.io/gh/sgorski00/nethelt/graph/badge.svg?token=Q49QZKE7OD)](https://codecov.io/gh/sgorski00/nethelt)

# NetHelt

Network monitoring platform consisting of a Spring Boot backend, Angular web application, desktop client, and AI-based anomaly detection module.

## Project structure

| Directory | Description |
|-----------|-------------|
| `core` | Shared models and interfaces used by `web-api` and `desktop-client` (Device, Result, NetworkOperation) |
| `web-api` | Spring Boot backend exposing the REST API, managing users, devices, notifications, and receiving health check results from clients |
| `desktop-client` | Desktop application with background service (bg-client) and GUI (gui-client) |
| `ai-model` | Python module for network anomaly detection using Isolation Forest |
| `frontend` | Angular web application for monitoring networks, devices, metrics, and user management |
| `infrastructure` | Docker Compose configuration and environment setup |
| `docs` | Project documentation |
| `coverage-aggregator` | JaCoCo test coverage aggregator |

## Requirements

- Java 25 or higher
- Python 3.12 or higher
- Docker and Docker Compose
- Node.js 22+, npm, Angular 21+ CLI

## Quick Start

Clone the repository:

```bash
git clone https://github.com/sgorski00/nethelt.git
cd nethelt
```

### Using Docker (recommended)

See [infrastructure/README.md](infrastructure/README.md) for Docker setup.

### Manual build

```bash
# Build everything
mvn clean install

# Build only web-api
mvn clean install -pl web-api -am

# Build only desktop-client
mvn clean install -pl desktop-client -am

# Generate coverage report
mvn verify -P coverage

# Start frontend
cd frontend
npm install
ng serve
```

## Status

| Module | Status         |
|--------|----------------|
| core | Stable         |
| bg-client | In development |
| web-api | In development    |
| ai-model | In development    |
| gui-client | Planned        |
| frontend | In development    |

## Author

[sgorski00](https://github.com/sgorski00)

## License

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for details.
