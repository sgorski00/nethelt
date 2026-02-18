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

## Setup

### Maven profiles

There are several Maven profiles, each of them is used for a different purpose:

- `client` - Used for building the desktop client
- `web` - Used for building the web API
- `full` - Used for building web API and desktop client together
- `coverage` - Used for generating code coverage reports (builds everything)

To buiild any of the modules, you need to specify the profile. For example, to build the desktop client, run:

```bash
mvn clean install -Pclient
```

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
