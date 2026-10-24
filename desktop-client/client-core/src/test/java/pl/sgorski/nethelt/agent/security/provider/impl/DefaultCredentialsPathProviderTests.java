package pl.sgorski.nethelt.agent.security.provider.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class DefaultCredentialsPathProviderTests {

  @Test
  void get_shouldReturnCustomPath_whenConfigured() {
    var provider =
        new DefaultCredentialsPathProvider("/tmp/nethelt/credentials", "Linux", _ -> null);

    assertEquals(Path.of("/tmp/nethelt/credentials"), provider.get());
  }

  @Test
  void get_shouldReturnEtcPath_onLinux() {
    var provider = new DefaultCredentialsPathProvider("", "Linux", _ -> null);

    assertEquals(Path.of("/etc/nethelt/credentials"), provider.get());
  }

  @Test
  void get_shouldReturnApplicationSupportPath_onMac() {
    var provider = new DefaultCredentialsPathProvider("", "Mac OS X", _ -> null);

    assertEquals(Path.of("/Library/Application Support/NetHelt/credentials"), provider.get());
  }

  @Test
  void get_shouldReturnProgramDataPath_onWindows() {
    var env = Map.of("ProgramData", "D:\\ProgramData");
    var provider = new DefaultCredentialsPathProvider("", "Windows 11", env::get);

    assertEquals(Path.of("D:\\ProgramData", "NetHelt", "credentials"), provider.get());
  }

  @Test
  void get_shouldFallbackToDefaultProgramData_onWindows_whenEnvIsMissing() {
    var provider = new DefaultCredentialsPathProvider("", "Windows 11", _ -> null);

    assertEquals(Path.of("C:\\ProgramData", "NetHelt", "credentials"), provider.get());
  }
}
