package pl.sgorski.nethelt.agent.uninstall;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Starts the Inno Setup uninstaller and returns without waiting, because files of this running
 * process cannot be deleted on Windows.
 */
public final class WindowsUninstaller implements Uninstaller {

  // Integrity levels: High (elevated administrator) and System
  private static final List<String> ELEVATED_SIDS = List.of("S-1-16-12288", "S-1-16-16384");

  private final Path uninstaller;

  public WindowsUninstaller(Path uninstaller) {
    this.uninstaller = uninstaller;
  }

  @Override
  public void verifyPrivileges() {
    var groups = Processes.output("whoami", "/groups");
    if (ELEVATED_SIDS.stream().noneMatch(groups::contains)) {
      throw new UninstallException(
          "Uninstalling NetHelt requires administrator rights."
              + " Run nethelt uninstall in a terminal opened as administrator.");
    }
  }

  @Override
  public void verifyInstallation() {
    if (!Files.isRegularFile(uninstaller)) {
      throw new UninstallException(
          "Uninstaller "
              + uninstaller
              + " was not found. Remove NetHelt from Settings > Apps instead.");
    }
  }

  @Override
  public int uninstall(boolean purge, boolean quiet) {
    try {
      new ProcessBuilder(command(purge, quiet))
          .redirectOutput(ProcessBuilder.Redirect.DISCARD)
          .redirectError(ProcessBuilder.Redirect.DISCARD)
          .start();
    } catch (IOException e) {
      throw new UninstallException("Failed to start " + uninstaller, e);
    }
    System.out.println("NetHelt uninstaller was started and continues in the background.");
    return 0;
  }

  List<String> command(boolean purge, boolean quiet) {
    var command =
        new ArrayList<>(
            List.of(
                uninstaller.toString(), quiet ? "/VERYSILENT" : "/SILENT", "/SUPPRESSMSGBOXES"));
    if (purge) {
      command.add("/PURGE");
    }
    return command;
  }
}
