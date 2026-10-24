package pl.sgorski.nethelt.agent.security.provider.impl;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.function.UnaryOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.security.provider.CredentialsPathProvider;

@Component
public final class DefaultCredentialsPathProvider implements CredentialsPathProvider {

  private static final String FILE_NAME = "credentials";
  private static final String APP_DIR_NAME = "NetHelt";
  private static final String DEFAULT_WINDOWS_PROGRAM_DATA = "C:\\ProgramData";
  private static final String DEFAULT_LINUX_PROGRAM_DATA = "/etc";
  private static final String DEFAULT_MAC_APPLICATION_SUPPORT = "/Library/Application Support";

  private final Path path;

  @Autowired
  public DefaultCredentialsPathProvider(@Value("${nethelt.credentials.path:}") String customPath) {
    this(customPath, System.getProperty("os.name"), System::getenv);
  }

  DefaultCredentialsPathProvider(String customPath, String osName, UnaryOperator<String> env) {
    this.path = customPath.isBlank() ? defaultPath(osName, env) : Path.of(customPath);
  }

  @Override
  public Path get() {
    return path;
  }

  private static Path defaultPath(String osName, UnaryOperator<String> env) {
    var os = osName.toLowerCase(Locale.ROOT);
    if (os.startsWith("windows")) {
      var programData =
          Objects.requireNonNullElse(env.apply("ProgramData"), DEFAULT_WINDOWS_PROGRAM_DATA);
      return Path.of(programData, APP_DIR_NAME, FILE_NAME);
    }
    if (os.startsWith("mac")) {
      return Path.of(DEFAULT_MAC_APPLICATION_SUPPORT, APP_DIR_NAME, FILE_NAME);
    }
    return Path.of(DEFAULT_LINUX_PROGRAM_DATA, APP_DIR_NAME.toLowerCase(Locale.ROOT), FILE_NAME);
  }
}
