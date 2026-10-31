package pl.sgorski.nethelt.agent.uninstall;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class WindowsUninstallerTests {

  private static final Path UNINSTALLER = Path.of("unins000.exe");

  @TempDir private Path tempDir;

  @Test
  void command_shouldRunSilent_whenNotQuiet() {
    assertEquals(
        List.of("unins000.exe", "/SILENT", "/SUPPRESSMSGBOXES"),
        new WindowsUninstaller(UNINSTALLER).command(false, false));
  }

  @Test
  void command_shouldRunVerySilentWithPurge_whenQuietAndPurge() {
    assertEquals(
        List.of("unins000.exe", "/VERYSILENT", "/SUPPRESSMSGBOXES", "/PURGE"),
        new WindowsUninstaller(UNINSTALLER).command(true, true));
  }

  @Test
  void verifyInstallation_shouldPass_whenUninstallerExists() throws IOException {
    var uninstaller = Files.createFile(tempDir.resolve("unins000.exe"));

    assertDoesNotThrow(() -> new WindowsUninstaller(uninstaller).verifyInstallation());
  }

  @Test
  void verifyInstallation_shouldThrow_whenUninstallerIsMissing() {
    var uninstaller = tempDir.resolve("unins000.exe");

    assertThrows(
        UninstallException.class, () -> new WindowsUninstaller(uninstaller).verifyInstallation());
  }
}
