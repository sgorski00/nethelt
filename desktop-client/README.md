# NetHelt - Desktop Client

Desktop application for network health monitoring. Contains background service and GUI client.

## Modules

| Module       | Description |
|--------------|-------------|
| `client-app` | Background service that periodically checks network devices and sends results to web-api |
| `gui-client` | Desktop GUI application (planned) |

## Building

```bash
mvn clean install -pl desktop-client -am
```

## Building and running background client

To build background client, you can run:

```bash
mvn clean install -pl :client-app -am
```

This command will generate your os-type specific executable in `client-app/target/packed/exe` directory. You can run it directly from there.

On Windows application will be built as a `.exe` service.
