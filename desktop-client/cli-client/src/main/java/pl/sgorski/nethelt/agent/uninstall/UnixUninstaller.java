package pl.sgorski.nethelt.agent.uninstall;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs the copy of {@code install.sh} kept in the install directory, so the uninstall logic lives
 * only in the script.
 */
public final class UnixUninstaller implements Uninstaller {

  /** Same as INSTALL_DIR in install.sh */
  public static final Path DEFAULT_SCRIPT = Path.of("/opt/nethelt/install.sh");

  private final Path script;

  public UnixUninstaller(Path script) {
    this.script = script;
  }

  @Override
  public void verifyPrivileges() {
    if (!"0".equals(Processes.output("id", "-u").strip())) {
      throw new UninstallException(
          "Uninstalling NetHelt requires root privileges. Run: sudo nethelt uninstall");
    }
  }

  @Override
  public void verifyInstallation() {
    if (!Files.isRegularFile(script)) {
      throw new UninstallException(
          "Uninstall script "
              + script
              + " was not found. Remove NetHelt with: "
              + "curl -fsSL https://github.com/sgorski00/nethelt/releases/latest/download/install.sh"
              + " | sudo bash -s -- --uninstall");
    }
  }

  @Override
  public int uninstall(boolean purge, boolean quiet) {
    try {
      // Deleting files of this running process
      return new ProcessBuilder(command(purge)).inheritIO().start().waitFor();
    } catch (IOException e) {
      throw new UninstallException("Failed to run " + script, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new UninstallException("Interrupted while running " + script, e);
    }
  }

  List<String> command(boolean purge) {
    var command = new ArrayList<>(List.of("/bin/bash", script.toString(), "--uninstall"));
    if (purge) {
      command.add("--purge");
    }
    return command;
  }
}
