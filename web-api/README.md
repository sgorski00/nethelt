# NetHelt Web API

Spring Boot REST API for the NetHelt project. 

## Features

- Authentication and authorization (JWT)
- OAuth2 providers support: register/login/link accounts
- Multi-channel Notifications
- [wip] Networks management
- [wip] Configuring devices in network
- [wip] Configuring desktop agent per network
- [wip] Per device stats configuration and metrics

## Tech Stack

- Java 25
- Spring Boot 4
- Spring Security
- Spring Data JPA
- PostgreSQL
- Maven

## Building

Build the module together with required dependencies:
```bash
mvn clean install -pl web-api -am
```

## Running

### Using Docker (recommended)

See [infrastructure/README.md](../infrastructure/README.md).

### Manual

Build the application first:
```bash
mvn clean install -pl web-api -am
```

Run:
```bash
java -jar target/web-api-1.0-SNAPSHOT.jar
```

The API runs on port 8080 by default.

## Dependencies

- `core` - shared models (Device, Result, NetworkConfig)
