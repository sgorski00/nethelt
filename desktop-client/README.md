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
sudo nethelt uninstall [--purge] [--yes]                          # the same with the CLI, it runs /opt/nethelt/install.sh --uninstall
sudo desktop-client/distribution/src/unix/install.sh --archive-dir desktop-client/distribution/target # local `make dist` build
```

The CLI and the background service are always installed. Optional components are listed in `OPTIONAL_COMPONENTS` at the top of the script and are selected interactively or with `--with <name>`. Each one is released as `nethelt-<name>-<os>-<arch>.tar.gz` and extracted on top of the core files.

### Windows

Run `nethelt-setup-<version>-windows-x64.exe` (needs administrator rights). The installer:

* installs to `C:\Program Files\NetHelt` and adds it to the system `PATH` (`nethelt` works in a new terminal),
* registers the `nethelt-agent` Windows service (WinSW wrapper `nethelt-agent-service.exe`, runs as LocalSystem, starts with the system),
* optionally registers the PAT (the page is skipped on upgrades when a PAT is already registered),
* restricts `C:\ProgramData\NetHelt` (credentials and logs) to SYSTEM and Administrators.

The CLI and the background service form the required `core` component. Optional components are prepared in `distribution/src/windows/nethelt.iss`.

Installing a newer version upgrades in place. Uninstall it with `nethelt uninstall [--purge] [--yes]` in a terminal run as administrator (it starts `unins000.exe` and returns), from *Settings > Apps* or with `unins000.exe` in the install directory, which asks whether to remove the PAT and logs.

```powershell
nethelt-setup-1.2.3-windows-x64.exe /VERYSILENT /SUPPRESSMSGBOXES   # silent install
& "C:\Program Files\NetHelt\unins000.exe" /VERYSILENT /PURGE      # silent uninstall, /PURGE also removes PAT and logs
```

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

Windows (PowerShell):

```powershell
Get-Service nethelt-agent                                                   # Status: Running
Get-Content C:\ProgramData\NetHelt\logs\nethelt-agent-service.out.log -Wait -Tail 50   # live logs (as administrator)
Restart-Service nethelt-agent                                               # restart (as administrator)
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

On Windows the build also creates the installer and needs [Inno Setup 6](https://jrsoftware.org/isinfo.php). WinSW is downloaded during the build and verified against `winsw.sha256`. Use `-Discc.executable=<path to ISCC.exe>` when Inno Setup is not installed in the default location, or `-DskipInstaller` to build only the app image and archive.

Output in `distribution/target`:

| Path | Description |
|------|-------------|
| `app-image/` | jpackage app image: one shared jlink runtime and two launchers, `nethelt` (CLI) and `nethelt-agent` (background service) |
| `nethelt-core-<os>-<arch>.tar.gz` / `.zip` (Windows) | Required component archive, input for `install.sh` (the Windows zip is only a local by-product, the installer uses `app-image/`) |
| `nethelt-setup-<version>-windows-x64.exe` | Windows installer (Windows only) |

Files of every launcher are named after it (`bin/nethelt-agent`, `nethelt-agent.exe`, `app/nethelt-agent.cfg`, `app/nethelt-agent.jar`). An optional component (for example a future `nethelt-gui`) is added as another jpackage launcher plus its own assembly descriptor, and excluded from `src/assembly/core.xml` by its name.

### JDK modules

The runtime contains only the modules listed in `jlink.modules` in `distribution/pom.xml`. After adding dependencies, check them again on the exec jars:

```bash
mkdir -p /tmp/jdeps && cd /tmp/jdeps && unzip -qo <path-to>/bg-client-*-exec.jar
for j in BOOT-INF/classes BOOT-INF/lib/*.jar; do
  jdeps --ignore-missing-deps --multi-release 25 -summary "$j" 2>/dev/null
done | grep -oE -- '-> (java|jdk)\.[a-z.]+' | sort -u
```

## Releases

`.github/workflows/desktop-client-release.yml` builds every platform on its own runner (jpackage and Inno Setup cannot cross-compile): `linux-x64`, `linux-arm64`, `macos-arm64` and `windows-x64`. Each job also installs the result, checks that the service runs and uninstalls it (Windows with `/PURGE`).

| Trigger | Result |
|---------|--------|
| Tag `client-v<MAJOR.MINOR.PATCH>` | GitHub release with the Linux/macOS archives, the Windows installer, `install.sh` and `SHA256SUMS` (the Windows core zip is not published) |
| Pull request changing `desktop-client/**` | Workflow artifact `nethelt-desktop-client-1.0.0` |
| Manual run (*Actions > Desktop client release > Run workflow*) | Workflow artifact `nethelt-desktop-client-<version>` |

The Web API URL of releases comes from the `NETHELT_API_URL` repository variable (*Settings > Secrets and variables > Actions > Variables*). Releases fail without it, pull requests and manual runs fall back to `http://localhost:8080/api`.

```bash
git tag client-v1.2.3
git push origin client-v1.2.3
```

To test a workflow artifact on Linux/macOS, unzip it and run `sudo bash install.sh --archive-dir .` in its directory (unzipping drops the executable bit, checksums are verified with the included `SHA256SUMS`).

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
