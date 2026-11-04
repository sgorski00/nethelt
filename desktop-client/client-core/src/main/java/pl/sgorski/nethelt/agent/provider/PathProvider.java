package pl.sgorski.nethelt.agent.provider;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.function.UnaryOperator;

public interface PathProvider {

  String APP_DIR_NAME = "NetHelt";
  String DEFAULT_WINDOWS_PROGRAM_DATA = "C:\\ProgramData";
  String DEFAULT_MAC_APPLICATION_SUPPORT = "/Library/Application Support";

  Path get();

  static Path defaultPath(
      String fileName, String linuxBaseDir, String osName, UnaryOperator<String> env) {
    var os = osName.toLowerCase(Locale.ROOT);
    if (os.startsWith("windows")) {
      var programData =
          Objects.requireNonNullElse(env.apply("ProgramData"), DEFAULT_WINDOWS_PROGRAM_DATA);
      return Path.of(programData, APP_DIR_NAME, fileName);
    }
    if (os.startsWith("mac")) {
      return Path.of(DEFAULT_MAC_APPLICATION_SUPPORT, APP_DIR_NAME, fileName);
    }
    return Path.of(linuxBaseDir, APP_DIR_NAME.toLowerCase(Locale.ROOT), fileName);
  }
}
