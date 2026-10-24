#!/usr/bin/env bash
#
# NetHelt desktop client installer for Linux (systemd) and macOS (launchd).
#
#   curl -fsSL https://github.com/sgorski00/nethelt/releases/latest/download/install.sh | sudo bash
#   curl -fsSL .../install.sh | sudo bash -s -- --yes --with <component>
#   curl -fsSL .../install.sh | sudo bash -s -- --uninstall [--purge]
#
# Running it again upgrades the installation. Keep it compatible with bash 3.2 (macOS).

set -euo pipefail

# ---------------------------------------------------------------------------
# Components. The core (CLI + background service) is always installed.
# Optional components are "<name>|<description>" entries. Each one is released as
# nethelt-<name>-<os>-<arch>.tar.gz and extracted on top of the core files.
# Example for the future GUI: "gui|Desktop GUI application"
# ---------------------------------------------------------------------------
OPTIONAL_COMPONENTS=()

RELEASES_URL="https://github.com/sgorski00/nethelt/releases"
RELEASE_TAG_PREFIX="client-v"
INSTALL_DIR="/opt/nethelt"
BIN_LINK="/usr/local/bin/nethelt"
COMPONENTS_FILE="components"

SERVICE_USER="nethelt"
LINUX_CONFIG_DIR="/etc/nethelt"
LINUX_UNIT_FILE="/etc/systemd/system/nethelt-agent.service"
MACOS_CONFIG_DIR="/Library/Application Support/NetHelt"
MACOS_LOG_DIR="/Library/Logs/NetHelt"
MACOS_LABEL="pl.sgorski.nethelt.agent"
MACOS_PLIST_FILE="/Library/LaunchDaemons/${MACOS_LABEL}.plist"

# Options
ASSUME_YES=false
VERSION=""
ARCHIVE_DIR=""
ACTION="install"
PURGE=false
REQUESTED_COMPONENTS=""

# Set by detect_platform
OS=""
ARCH=""

WORK_DIR=""
STAGE_DIR=""

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

# Messages go to stderr, stdout of some functions is captured
info() { printf '==> %s\n' "$*" >&2; }
warn() { printf 'Warning: %s\n' "$*" >&2; }
die() {
  printf 'Error: %s\n' "$*" >&2
  exit 1
}

usage() {
  cat <<EOF
Usage: install.sh [options]

Installs or upgrades the NetHelt desktop client (CLI and background service).

Options:
  -y, --yes             Do not ask questions, install only required and --with components
      --with NAME       Install an optional component (can be repeated)
      --version VER     Install a specific release (e.g. 1.2.3) instead of the latest one
      --archive-dir DIR Use archives from a local directory instead of downloading them
      --uninstall       Remove the client and its service
      --purge           With --uninstall, also remove credentials and the service account
  -h, --help            Show this help
EOF
  local names
  names=$(optional_component_names)
  if [ -n "$names" ]; then
    printf '\nOptional components: %s\n' "$names"
  fi
}

component_name() { printf '%s' "${1%%|*}"; }
component_description() { printf '%s' "${1#*|}"; }

optional_component_names() {
  local entry names=""
  for entry in ${OPTIONAL_COMPONENTS[@]+"${OPTIONAL_COMPONENTS[@]}"}; do
    names="$names $(component_name "$entry")"
  done
  printf '%s' "${names# }"
}

is_optional_component() {
  case " $(optional_component_names) " in
    *" $1 "*) return 0 ;;
    *) return 1 ;;
  esac
}

contains_word() {
  case " $1 " in
    *" $2 "*) return 0 ;;
    *) return 1 ;;
  esac
}

# Questions are read from the terminal, because stdin is the script itself with "curl | bash"
has_tty() { [ -r /dev/tty ] && [ -w /dev/tty ] && { : </dev/tty; } 2>/dev/null; }

ask_yes_no() {
  local question=$1 default=$2 hint answer
  if [ "$default" = "y" ]; then hint="[Y/n]"; else hint="[y/N]"; fi
  printf '%s %s ' "$question" "$hint" >/dev/tty
  read -r answer </dev/tty || answer=""
  case "${answer:-$default}" in
    y | Y | yes | YES) return 0 ;;
    *) return 1 ;;
  esac
}

cleanup() {
  local dir
  for dir in "$WORK_DIR" "$STAGE_DIR"; do
    if [ -n "$dir" ] && [ -d "$dir" ]; then
      rm -rf "$dir"
    fi
  done
}

# ---------------------------------------------------------------------------
# Platform
# ---------------------------------------------------------------------------

detect_platform() {
  case "$(uname -s)" in
    Linux) OS="linux" ;;
    Darwin) OS="macos" ;;
    *) die "Unsupported operating system: $(uname -s). Use the Windows installer on Windows." ;;
  esac
  case "$(uname -m)" in
    x86_64 | amd64) ARCH="x64" ;;
    aarch64 | arm64) ARCH="arm64" ;;
    *) die "Unsupported architecture: $(uname -m)" ;;
  esac
  if [ "$OS" = "linux" ] && ls /lib/ld-musl-* >/dev/null 2>&1; then
    die "musl based distributions (e.g. Alpine) are not supported, glibc is required."
  fi
}

require_root() {
  if [ "$(id -u)" -ne 0 ]; then
    die "This installer needs root privileges. Run it with sudo."
  fi
}

has_systemd() { [ -d /run/systemd/system ] && command -v systemctl >/dev/null 2>&1; }

launcher_path() {
  if [ "$OS" = "macos" ]; then
    printf '%s/nethelt.app/Contents/MacOS/%s' "$INSTALL_DIR" "$1"
  else
    printf '%s/bin/%s' "$INSTALL_DIR" "$1"
  fi
}

# ---------------------------------------------------------------------------
# Arguments
# ---------------------------------------------------------------------------

parse_args() {
  while [ $# -gt 0 ]; do
    case "$1" in
      -y | --yes) ASSUME_YES=true ;;
      --with)
        [ $# -ge 2 ] || die "--with needs a component name"
        is_optional_component "$2" || die "Unknown component '$2'. Available: $(optional_component_names | grep . || echo none)"
        REQUESTED_COMPONENTS="$REQUESTED_COMPONENTS $2"
        shift
        ;;
      --version)
        [ $# -ge 2 ] || die "--version needs a value"
        VERSION="${2#"$RELEASE_TAG_PREFIX"}"
        VERSION="${VERSION#v}"
        shift
        ;;
      --archive-dir)
        [ $# -ge 2 ] || die "--archive-dir needs a directory"
        [ -d "$2" ] || die "Directory not found: $2"
        ARCHIVE_DIR=$(cd "$2" && pwd)
        shift
        ;;
      --uninstall) ACTION="uninstall" ;;
      --purge) PURGE=true ;;
      -h | --help)
        usage
        exit 0
        ;;
      *) die "Unknown option: $1 (see --help)" ;;
    esac
    shift
  done
  if [ "$PURGE" = true ] && [ "$ACTION" != "uninstall" ]; then
    die "--purge can be used only with --uninstall"
  fi
}

# ---------------------------------------------------------------------------
# Component selection
# ---------------------------------------------------------------------------

installed_components() {
  if [ -f "$INSTALL_DIR/$COMPONENTS_FILE" ]; then
    tr '\n' ' ' <"$INSTALL_DIR/$COMPONENTS_FILE"
  fi
}

select_components() {
  local selected="$REQUESTED_COMPONENTS" previous entry name default
  previous=$(installed_components)

  info "Required components: CLI (nethelt), background service (nethelt-agent)"
  for entry in ${OPTIONAL_COMPONENTS[@]+"${OPTIONAL_COMPONENTS[@]}"}; do
    name=$(component_name "$entry")
    contains_word "$selected" "$name" && continue
    if [ "$ASSUME_YES" = true ] || ! has_tty; then
      # Upgrades keep what was installed before
      contains_word "$previous" "$name" && selected="$selected $name"
      continue
    fi
    default="n"
    contains_word "$previous" "$name" && default="y"
    if ask_yes_no "Install optional component: $(component_description "$entry")?" "$default"; then
      selected="$selected $name"
    fi
  done
  printf '%s' "${selected# }"
}

# ---------------------------------------------------------------------------
# Download and verification
# ---------------------------------------------------------------------------

archive_name() { printf 'nethelt-%s-%s-%s.tar.gz' "$1" "$OS" "$ARCH"; }

release_url() {
  if [ -n "$VERSION" ]; then
    printf '%s/download/%s%s/%s' "$RELEASES_URL" "$RELEASE_TAG_PREFIX" "$VERSION" "$1"
  else
    printf '%s/latest/download/%s' "$RELEASES_URL" "$1"
  fi
}

download() {
  local url=$1 target=$2
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL --retry 3 -o "$target" "$url"
  elif command -v wget >/dev/null 2>&1; then
    wget -q -O "$target" "$url"
  else
    die "curl or wget is required to download NetHelt"
  fi
}

sha256_of() {
  if command -v sha256sum >/dev/null 2>&1; then
    sha256sum "$1" | awk '{print $1}'
  else
    shasum -a 256 "$1" | awk '{print $1}'
  fi
}

verify_checksum() {
  local file=$1 sums=$2 name expected actual
  name=$(basename "$file")
  expected=$(awk -v f="$name" '$2 == f || $2 == "*" f {print $1; exit}' "$sums")
  [ -n "$expected" ] || die "No checksum for $name in SHA256SUMS"
  actual=$(sha256_of "$file")
  [ "$expected" = "$actual" ] || die "Checksum mismatch for $name"
}

# Fetches archives of the core and the given components into WORK_DIR, prints their paths
fetch_archives() {
  local components=$1 parts="core" part name sums=""
  for part in $components; do parts="$parts $part"; done

  if [ -n "$ARCHIVE_DIR" ]; then
    [ -f "$ARCHIVE_DIR/SHA256SUMS" ] && sums="$ARCHIVE_DIR/SHA256SUMS"
    [ -n "$sums" ] || warn "No SHA256SUMS in $ARCHIVE_DIR, checksums are not verified"
  else
    sums="$WORK_DIR/SHA256SUMS"
    info "Downloading NetHelt ${VERSION:-latest} for $OS-$ARCH"
    download "$(release_url SHA256SUMS)" "$sums" ||
      die "Cannot download $(release_url SHA256SUMS). Check that release '${VERSION:-latest}' exists, or install a local build with --archive-dir <dir>."
  fi

  for part in $parts; do
    name=$(archive_name "$part")
    if [ -n "$ARCHIVE_DIR" ]; then
      [ -f "$ARCHIVE_DIR/$name" ] || die "Archive not found: $ARCHIVE_DIR/$name"
      cp "$ARCHIVE_DIR/$name" "$WORK_DIR/$name"
    else
      download "$(release_url "$name")" "$WORK_DIR/$name" || die "Cannot download $(release_url "$name")"
    fi
    [ -n "$sums" ] && verify_checksum "$WORK_DIR/$name" "$sums"
    printf '%s\n' "$WORK_DIR/$name"
  done
}

# ---------------------------------------------------------------------------
# Files
# ---------------------------------------------------------------------------

# Builds the new installation in STAGE_DIR/nethelt
stage_installation() {
  local archives=$1 components=$2 stage=$STAGE_DIR archive component
  mkdir -p "$stage/nethelt"
  for archive in $archives; do
    if [ "$OS" = "macos" ]; then
      # nethelt.app/... goes into INSTALL_DIR
      tar -xzf "$archive" --no-same-owner -C "$stage/nethelt"
    else
      # nethelt/bin, nethelt/lib become INSTALL_DIR/bin, INSTALL_DIR/lib
      tar -xzf "$archive" --no-same-owner -C "$stage"
    fi
  done
  : >"$stage/nethelt/$COMPONENTS_FILE"
  for component in $components; do
    printf '%s\n' "$component" >>"$stage/nethelt/$COMPONENTS_FILE"
  done
  if [ "$OS" = "macos" ]; then
    chown -R root:wheel "$stage/nethelt"
    xattr -dr com.apple.quarantine "$stage/nethelt" 2>/dev/null || true
  else
    chown -R root:root "$stage/nethelt"
  fi
  chmod -R go-w "$stage/nethelt"
  chmod 755 "$stage/nethelt"
}

swap_installation() {
  if [ -d "$INSTALL_DIR" ]; then
    rm -rf "$INSTALL_DIR.old"
    mv "$INSTALL_DIR" "$INSTALL_DIR.old"
  fi
  mv "$STAGE_DIR/nethelt" "$INSTALL_DIR"
  rm -rf "$STAGE_DIR" "$INSTALL_DIR.old"
}

link_cli() {
  mkdir -p "$(dirname "$BIN_LINK")"
  ln -sfn "$(launcher_path nethelt)" "$BIN_LINK"
}

# ---------------------------------------------------------------------------
# Service: Linux (systemd)
# ---------------------------------------------------------------------------

linux_create_service_account() {
  if id "$SERVICE_USER" >/dev/null 2>&1; then
    return
  fi
  info "Creating system user '$SERVICE_USER'"
  local nologin
  nologin=$(command -v nologin || echo /usr/sbin/nologin)
  if command -v useradd >/dev/null 2>&1; then
    useradd --system --user-group --no-create-home --home-dir /nonexistent --shell "$nologin" "$SERVICE_USER"
  else
    die "useradd is required to create the '$SERVICE_USER' system user"
  fi
}

# Credentials file gets the group of this directory (see FileCredentialsStore), so the service can read it
linux_prepare_config_dir() {
  mkdir -p "$LINUX_CONFIG_DIR"
  chown "root:$SERVICE_USER" "$LINUX_CONFIG_DIR"
  chmod 0750 "$LINUX_CONFIG_DIR"
  if [ -f "$LINUX_CONFIG_DIR/credentials" ]; then
    chgrp "$SERVICE_USER" "$LINUX_CONFIG_DIR/credentials"
    chmod 0640 "$LINUX_CONFIG_DIR/credentials"
  fi
}

linux_write_unit() {
  cat >"$LINUX_UNIT_FILE" <<EOF
[Unit]
Description=NetHelt monitoring agent
Wants=network-online.target
After=network-online.target

[Service]
Type=simple
User=$SERVICE_USER
Group=$SERVICE_USER
ExecStart=$(launcher_path nethelt-agent)
Restart=on-failure
RestartSec=10
# Raw ICMP sockets for ping, Java falls back to TCP port 7 without it
AmbientCapabilities=CAP_NET_RAW
CapabilityBoundingSet=CAP_NET_RAW
NoNewPrivileges=true
ProtectSystem=strict
ProtectHome=true
PrivateTmp=true

[Install]
WantedBy=multi-user.target
EOF
  chmod 0644 "$LINUX_UNIT_FILE"
}

linux_stop_service() {
  if has_systemd && systemctl is-active --quiet nethelt-agent 2>/dev/null; then
    info "Stopping nethelt-agent service"
    systemctl stop nethelt-agent
  fi
}

linux_start_service() {
  if ! has_systemd; then
    warn "systemd is not running, the service was not started. Start $(launcher_path nethelt-agent) as '$SERVICE_USER' yourself."
    return
  fi
  systemctl daemon-reload
  systemctl enable --quiet nethelt-agent
  systemctl restart nethelt-agent
  info "Service nethelt-agent is enabled and running"
}

linux_uninstall_service() {
  if has_systemd && [ -f "$LINUX_UNIT_FILE" ]; then
    systemctl disable --now --quiet nethelt-agent 2>/dev/null || true
  fi
  rm -f "$LINUX_UNIT_FILE"
  if has_systemd; then
    systemctl daemon-reload
  fi
}

# ---------------------------------------------------------------------------
# Service: macOS (launchd)
# ---------------------------------------------------------------------------

# Created up front, otherwise it inherits the "admin" group of its parent (BSD semantics)
macos_prepare_config_dir() {
  mkdir -p "$MACOS_CONFIG_DIR"
  chown root:wheel "$MACOS_CONFIG_DIR"
  chmod 0750 "$MACOS_CONFIG_DIR"
  if [ -f "$MACOS_CONFIG_DIR/credentials" ]; then
    chgrp wheel "$MACOS_CONFIG_DIR/credentials"
    chmod 0640 "$MACOS_CONFIG_DIR/credentials"
  fi
}

macos_write_plist() {
  mkdir -p "$MACOS_LOG_DIR"
  cat >"$MACOS_PLIST_FILE" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
  <key>Label</key>
  <string>$MACOS_LABEL</string>
  <key>ProgramArguments</key>
  <array>
    <string>$(launcher_path nethelt-agent)</string>
  </array>
  <key>RunAtLoad</key>
  <true/>
  <key>KeepAlive</key>
  <true/>
  <key>StandardOutPath</key>
  <string>$MACOS_LOG_DIR/agent.log</string>
  <key>StandardErrorPath</key>
  <string>$MACOS_LOG_DIR/agent.log</string>
</dict>
</plist>
EOF
  chown root:wheel "$MACOS_PLIST_FILE"
  chmod 0644 "$MACOS_PLIST_FILE"
}

macos_stop_service() {
  local attempt=0
  if ! launchctl print "system/$MACOS_LABEL" >/dev/null 2>&1; then
    return
  fi
  info "Stopping nethelt-agent service"
  launchctl bootout "system/$MACOS_LABEL" 2>/dev/null || true
  # bootout returns before the job is gone, bootstrap fails until then
  while launchctl print "system/$MACOS_LABEL" >/dev/null 2>&1 && [ $attempt -lt 20 ]; do
    sleep 0.5
    attempt=$((attempt + 1))
  done
}

macos_start_service() {
  launchctl bootstrap system "$MACOS_PLIST_FILE"
  info "Service $MACOS_LABEL is loaded and running (logs: $MACOS_LOG_DIR/agent.log)"
}

macos_uninstall_service() {
  macos_stop_service
  rm -f "$MACOS_PLIST_FILE"
}

# ---------------------------------------------------------------------------
# Actions
# ---------------------------------------------------------------------------

stop_service() { if [ "$OS" = "macos" ]; then macos_stop_service; else linux_stop_service; fi; }

offer_pat_registration() {
  local cli status pat
  cli=$(launcher_path nethelt)
  status=$("$cli" auth status 2>/dev/null || true)
  case "$status" in
    *"Not registered"*) ;;
    *) return ;;
  esac
  if [ "$ASSUME_YES" = true ] || ! has_tty; then
    info "Register the agent with: sudo nethelt auth register"
    return
  fi
  printf 'Paste the Personal Access Token from the NetHelt web app (leave empty to skip): ' >/dev/tty
  read -rs pat </dev/tty || pat=""
  printf '\n' >/dev/tty
  if [ -z "$pat" ]; then
    info "Skipped. Register the agent later with: sudo nethelt auth register"
    return
  fi
  printf '%s\n' "$pat" | "$cli" auth register --stdin
}

install_client() {
  local components archives action="Installing"
  [ -d "$INSTALL_DIR" ] && action="Upgrading"

  components=$(select_components)
  WORK_DIR=$(mktemp -d)
  archives=$(fetch_archives "$components")

  info "$action NetHelt in $INSTALL_DIR"
  # Next to INSTALL_DIR, so the swap is a rename on the same filesystem
  mkdir -p "$(dirname "$INSTALL_DIR")"
  STAGE_DIR=$(mktemp -d "$(dirname "$INSTALL_DIR")/.nethelt-stage.XXXXXX")
  stage_installation "$archives" "$components"
  if [ "$OS" = "macos" ]; then
    macos_prepare_config_dir
  else
    linux_create_service_account
    linux_prepare_config_dir
  fi
  stop_service
  swap_installation
  link_cli

  if [ "$OS" = "macos" ]; then
    macos_write_plist
    macos_start_service
  else
    linux_write_unit
    linux_start_service
  fi

  offer_pat_registration
  info "Done. Run 'nethelt --help' to get started."
}

uninstall_client() {
  info "Removing NetHelt"
  if [ "$OS" = "macos" ]; then
    macos_uninstall_service
  else
    linux_uninstall_service
  fi
  if [ -L "$BIN_LINK" ]; then
    rm -f "$BIN_LINK"
  fi
  rm -rf "$INSTALL_DIR" "$INSTALL_DIR.old"

  if [ "$PURGE" = true ]; then
    info "Removing credentials and service account"
    if [ "$OS" = "macos" ]; then
      rm -rf "$MACOS_CONFIG_DIR" "$MACOS_LOG_DIR"
    else
      rm -rf "$LINUX_CONFIG_DIR"
      if id "$SERVICE_USER" >/dev/null 2>&1; then
        userdel "$SERVICE_USER" 2>/dev/null || warn "Cannot remove the '$SERVICE_USER' user"
      fi
    fi
  else
    info "Credentials were kept, use --purge to remove them"
  fi
  info "NetHelt was removed"
}

main() {
  parse_args "$@"
  detect_platform
  require_root
  trap cleanup EXIT
  if [ "$ACTION" = "uninstall" ]; then
    uninstall_client
  else
    install_client
  fi
}

main "$@"
