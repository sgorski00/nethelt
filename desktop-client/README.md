# NetHelt - Desktop Client

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

## Building and running background client

Tho build background client, you can run:

```bash
mvn clean install -pl :bg-client -am
```

This command will generate your os-type specific executable in `bg-client/target/packed/exe` directory. You can run it directly from there.

On Windows application will be built as a `.exe` service.

## Dependencies

- `core` - shared models (Device, Result, NetworkConfig)
