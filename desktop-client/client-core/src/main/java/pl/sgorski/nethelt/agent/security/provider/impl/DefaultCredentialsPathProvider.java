package pl.sgorski.nethelt.agent.security.provider.impl;

import java.nio.file.Path;
import java.util.function.UnaryOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.provider.PathProvider;

@Component(DefaultCredentialsPathProvider.BEAN_NAME)
public final class DefaultCredentialsPathProvider implements PathProvider {

  public static final String BEAN_NAME = "credentialsPathProvider";

  private static final String FILE_NAME = "credentials";
  private static final String LINUX_CONFIG_DIR = "/etc";

  private final Path path;

  @Autowired
  public DefaultCredentialsPathProvider(@Value("${nethelt.credentials.path:}") String customPath) {
    this(customPath, System.getProperty("os.name"), System::getenv);
  }

  DefaultCredentialsPathProvider(String customPath, String osName, UnaryOperator<String> env) {
    this.path =
        customPath.isBlank()
            ? PathProvider.defaultPath(FILE_NAME, LINUX_CONFIG_DIR, osName, env)
            : Path.of(customPath);
  }

  @Override
  public Path get() {
    return path;
  }
}
