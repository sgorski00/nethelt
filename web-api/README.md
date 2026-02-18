# Web API

Spring Boot REST API for the NetHelt project. 

## Features

- Adding devices
- Receiving health check results from clients
- (wip) Users management 
- (wip) Devices healthchecks configuration

## Building

```bash
mvn clean install -pl web-api -am
```

## Running

### Using Docker (recommended)

See [infrastructure/README.md](../infrastructure/README.md).

### Manual

```bash
java -jar target/web-api-1.0-SNAPSHOT.jar
```

The API runs on port 8080 by default.

## Dependencies

- `core` - shared models (Device, Result, NetworkConfig)
