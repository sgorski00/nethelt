# NetHelt - Desktop Client

Desktop application for network health monitoring. Contains background service and GUI client.

## Modules

| Module       | Description |
|--------------|-------------|
| `client-core` | Code shared by the other modules (credentials storage) |
| `bg-client` | Background service that periodically checks network devices and sends results to web-api |
| `cli-client` | Command line tool (`nethelt auth register\|status\|remove`) |
| `gui-client` | Desktop GUI application (planned) |

## Building

```bash
mvn clean install -pl desktop-client -am
```

## Building and running background client

To build background client, you can run:

```bash
mvn clean install -pl :bg-client -am
```

This command will generate your os-type specific executable in `bg-client/target/packed/exe` directory. You can run it directly from there.

On Windows application will be built as a `.exe` service.

## Credentials

The PAT is stored in a machine-wide file, so both the CLI and the background service (running as a system service) can use it:

| OS      | Path                                              |
|---------|---------------------------------------------------|
| Linux   | `/etc/nethelt/credentials`                        |
| Windows | `%ProgramData%\NetHelt\credentials`               |
| macOS   | `/Library/Application Support/NetHelt/credentials` |

Managing the PAT needs root/admin rights (`sudo nethelt auth register`). On Linux/macOS the file gets `rw-r-----` and the group of its directory, so the service account only needs to be in the directory's group. On Windows only SYSTEM and Administrators can access it.

`nethelt auth register --stdin` reads the PAT from standard input (for installers and scripts).

For local development set a custom location with the `NETHELT_CREDENTIALS_PATH` env variable (or `nethelt.credentials.path` property), for example:

```bash
export NETHELT_CREDENTIALS_PATH=$HOME/.nethelt/credentials
```
