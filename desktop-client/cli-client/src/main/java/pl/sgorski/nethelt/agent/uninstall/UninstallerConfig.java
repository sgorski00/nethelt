package pl.sgorski.nethelt.agent.uninstall;

import java.nio.file.Path;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UninstallerConfig {

  @Bean
  public Uninstaller uninstaller() {
    if (System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("windows")) {
      return new WindowsUninstaller(windowsInstallDir().resolve("unins000.exe"));
    }
    return new UnixUninstaller(UnixUninstaller.DEFAULT_SCRIPT);
  }

  // jpackage sets the launcher path, the user can change the install dir in the installer
  private static Path windowsInstallDir() {
    var appPath = System.getProperty("jpackage.app-path");
    if (appPath != null) {
      return Path.of(appPath).getParent();
    }
    return Path.of(System.getenv().getOrDefault("ProgramFiles", "C:\\Program Files"), "NetHelt");
  }
}
