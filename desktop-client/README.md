# NetHelt - Desktop Client

Desktop application for network health monitoring. Contains background service and GUI client.

## Modules

| Module       | Description |
|--------------|-------------|
| `client-core` | Code shared by the other modules (credentials storage) |
| `bg-client` | Background service that periodically checks network devices and sends results to web-api |
| `cli-client` | Command line tool (`nethelt auth register\|status\|remove`) |
| `gui-client` | Desktop GUI application (planned) |

## Installation

### Linux and macOS

```bash
curl -fsSL https://github.com/sgorski00/nethelt/releases/latest/download/install.sh | sudo bash
```

The script (`distribution/src/unix/install.sh`) downloads the archive for the current OS/architecture from GitHub Releases, verifies it against `SHA256SUMS` and installs it:

| | Linux | macOS |
|---|---|---|
| Application | `/opt/nethelt` | `/opt/nethelt/nethelt.app` |
| CLI | `/usr/local/bin/nethelt` | `/usr/local/bin/nethelt` |
| Service | systemd unit `nethelt-agent`, runs as system user `nethelt` with `CAP_NET_RAW` | launchd daemon `pl.sgorski.nethelt.agent`, logs in `/Library/Logs/NetHelt` |

At the end it asks for the PAT (it can be registered later with `sudo nethelt auth register`). Running it again upgrades the installation and keeps the credentials.

Options are passed after `bash -s --`:

```bash
curl -fsSL .../install.sh | sudo bash -s -- --yes                 # no questions
curl -fsSL .../install.sh | sudo bash -s -- --version 1.2.3       # specific release
curl -fsSL .../install.sh | sudo bash -s -- --uninstall [--purge] # remove (--purge also removes credentials and the service account)
sudo desktop-client/distribution/src/unix/install.sh --archive-dir desktop-client/distribution/target # local `make dist` build
```

The CLI and the background service are always installed. Optional components are listed in `OPTIONAL_COMPONENTS` at the top of the script and are selected interactively or with `--with <name>`. Each one is released as `nethelt-<name>-<os>-<arch>.tar.gz` and extracted on top of the core files.

### Checking the background service

Linux (systemd):

```bash
systemctl status nethelt-agent                   # "active (running)" and "enabled" (starts with the system)
journalctl -u nethelt-agent -f                   # live logs
journalctl -u nethelt-agent -n 50 --no-pager     # last 50 lines
sudo systemctl restart nethelt-agent             # restart (stop / start work the same way)
```

macOS (launchd):

```bash
sudo launchctl print system/pl.sgorski.nethelt.agent   # state and PID
tail -f /Library/Logs/NetHelt/agent.log                 # live logs
```

What the logs mean:

| Log message | Meaning |
|-------------|---------|
| `Skipping heartbeat: No Personal Access Token is registered` | Service runs, but no PAT is registered. Run `sudo nethelt auth register` |
| `Skipping ...: Access denied to credentials file` | The service account cannot read the credentials file, check the owner/group of the config directory |
| `Error while ... I/O error on POST request` | PAT is registered, but the Web API cannot be reached |
| No errors | Agent works, its status is also visible in the web app (heartbeat) |

## Building

```bash
mvn clean install -pl desktop-client -am
```

`bg-client` and `cli-client` produce Spring Boot executable jars with the `exec` classifier (`target/*-exec.jar`), which can be run with `java -jar`.

### Web API URL

The Web API URL is set at build time and cannot be changed by the user. It comes from the `nethelt.api-url` Maven property (default `http://localhost:8080/api`):

```bash
mvn clean package -Pdist -pl desktop-client/distribution -am -Dnethelt.api-url=https://nethelt.example.com/api
```

## Distribution

The `distribution` module is built only with the `dist` profile and needs a JDK with `jpackage` (Java 25). It builds a package for the current OS only, so every platform is built on its own machine/CI runner.

```bash
make dist
# or with a release version (numeric, required by jpackage on Windows)
mvn clean package -Pdist -pl desktop-client/distribution -am -DskipTests -Dapp.version=1.2.3
```

Output in `distribution/target`:

| Path | Description |
|------|-------------|
| `app-image/` | jpackage app image: one shared jlink runtime and two launchers, `nethelt` (CLI) and `nethelt-agent` (background service) |
| `nethelt-core-<os>-<arch>.tar.gz` / `.zip` (Windows) | Required component archive, input for the installers |

Files of every launcher are named after it (`bin/nethelt-agent`, `nethelt-agent.exe`, `app/nethelt-agent.cfg`, `app/nethelt-agent.jar`). An optional component (for example a future `nethelt-gui`) is added as another jpackage launcher plus its own assembly descriptor, and excluded from `src/assembly/core.xml` by its name.

### JDK modules

The runtime contains only the modules listed in `jlink.modules` in `distribution/pom.xml`. After adding dependencies, check them again on the exec jars:

```bash
mkdir -p /tmp/jdeps && cd /tmp/jdeps && unzip -qo <path-to>/bg-client-*-exec.jar
for j in BOOT-INF/classes BOOT-INF/lib/*.jar; do
  jdeps --ignore-missing-deps --multi-release 25 -summary "$j" 2>/dev/null
done | grep -oE -- '-> (java|jdk)\.[a-z.]+' | sort -u
```

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
