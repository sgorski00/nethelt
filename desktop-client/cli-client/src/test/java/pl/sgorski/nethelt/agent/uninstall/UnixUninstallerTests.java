package pl.sgorski.nethelt.agent.uninstall;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class UnixUninstallerTests {

  @TempDir private Path tempDir;

  @Test
  void command_shouldRunScriptWithUninstall() {
    var uninstaller = new UnixUninstaller(Path.of("/opt/nethelt/install.sh"));

    assertEquals(
        List.of("/bin/bash", "/opt/nethelt/install.sh", "--uninstall"), uninstaller.command(false));
  }

  @Test
  void command_shouldAddPurge_whenPurgeIsRequested() {
    var uninstaller = new UnixUninstaller(Path.of("/opt/nethelt/install.sh"));

    assertEquals(
        List.of("/bin/bash", "/opt/nethelt/install.sh", "--uninstall", "--purge"),
        uninstaller.command(true));
  }

  @Test
  void verifyInstallation_shouldPass_whenScriptExists() throws IOException {
    var script = Files.createFile(tempDir.resolve("install.sh"));

    assertDoesNotThrow(() -> new UnixUninstaller(script).verifyInstallation());
  }

  @Test
  void verifyInstallation_shouldThrow_whenScriptIsMissing() {
    var script = tempDir.resolve("install.sh");

    var exception =
        assertThrows(
            UninstallException.class, () -> new UnixUninstaller(script).verifyInstallation());
    assertTrue(exception.getMessage().contains(script.toString()));
  }
}
