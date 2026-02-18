# nethelt

Network health checker with web app and desktop client.

## Project structure

| Directory | Description |
|-----------|-------------|
| `core` | Shared models and interfaces used by `web-api` and `desktop-client` (Device, Result, NetworkOperation) |
| `web-api` | Spring Boot REST API - receives health check results from clients, serves configuration |
| `desktop-client` | Desktop application with background service (bg-client) and GUI (gui-client) |
| `ai-model` | Python module for network anomaly detection using Isolation Forest |
| `frontend` | Angular web dashboard (planned) |
| `infrastructure` | Docker Compose configuration and environment setup |
| `docs` | Project documentation |
| `coverage-aggregator` | JaCoCo test coverage aggregator |

## Requirements

- Java 25 or higher
- Python 3.12 or higher (for ai-model)
- Docker and Docker Compose
- Node.js 20+ and npm

## Quick Start

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
```

The `-am` flag automatically builds required dependencies.

## Status

| Module | Status |
|--------|--------|
| core | Basic implementation |
| bg-client | Basic implementation |
| web-api | In progress |
| ai-model | In progress |
| gui-client | Planned |
| frontend | Planned |

## Author

[sgorski00](https://github.com/sgorski00)

## License

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for details.
