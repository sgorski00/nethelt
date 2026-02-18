# nethelt

Network health checker with web app and desktop client.

## Project structure

- `ai-model` - AI model for network anomalies detection and prediction
- `web-api` - Backend API
- `frontend` - Web application that consumes the API
- `desktop-client` - Desktop application for network monitoring and alerts. Contains background service and user interface.
- `core` - Shared code and utilities used across `web-api`, and `desktop-client`
- `docs` - Documentation for the project, including setup instructions and user guides
- `coverage-aggregator` - Jacoco coverage report aggregator for combining reports from `core`, `web-api` and `desktop-client` into a single report for overall test coverage analysis.
- `infrastructure` - Docker containers settings, scripts, etc

## Requirements

- Java 25 or higher
- Python 3.11 or higher (for AI model development)
- Docker & Docker Compose
- Node.js 20 or higher

## Run & Setup

### Makefile

In the root directory of the project, there is a `Makefile` that provides convenient commands for building and running the project. Here are some of the available commands:
- `make up` - Builds and starts whole environemnt, including the web API
- `make down` - Stops and removes the environment
- `make build` - Builds the project using Maven
- `make logs` - Runs logs for all containers

### Maven profiles

Use `-pl` flag to build specific modules:

```bash
# Build everything
mvn clean install

# Build only web-api (with core dependency)
mvn clean install -pl core,web-api

# Build only desktop-client (with dependencies)
mvn clean install -pl desktop-client -am

# Generate coverage report
mvn verify -P coverage
```

The `-am` (also-make) flag automatically builds required dependencies.

## Status

| Module     | Status               |
|------------|----------------------|
| core       | Basic implementation |
| bg-client  | Basic implementation |
| web-api    | In progress          |
| ai-model   | In progress          |
| gui-client | Planned              |
| frontend   | Planned              |

## Author

- [sgorski00](https://github.com/sgorski00)

## License

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for details.
