# Desktop Client

Desktop application for network health monitoring. Contains background service and GUI client.

## Modules

| Module | Description |
|--------|-------------|
| `client-common` | Shared code for background and GUI clients (scheduler, serialization, executor) |
| `bg-client` | Background service that periodically checks network devices and sends results to web-api |
| `gui-client` | Desktop GUI application (planned) |
| `network` | Network operations implementation (ping, telnet) |

## Building

```bash
mvn clean install -pl desktop-client -am
```

## Running Background Client

```bash
java -jar bg-client/target/bg-client-1.0-SNAPSHOT.jar
```

## Dependencies

- `core` - shared models (Device, Result, NetworkConfig)
