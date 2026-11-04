package pl.sgorski.nethelt.agent.buffer;

import java.nio.file.Path;
import java.util.function.UnaryOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.provider.PathProvider;

@Component
public final class ResultBufferPathProvider implements PathProvider {

  private static final String FILE_NAME = "result-buffer.jsonl";
  private static final String LINUX_STATE_DIR = "/var/lib";

  private final Path path;

  @Autowired
  public ResultBufferPathProvider(ResultBufferProperties properties) {
    this(properties.path(), System.getProperty("os.name"), System::getenv);
  }

  ResultBufferPathProvider(String customPath, String osName, UnaryOperator<String> env) {
    this.path =
        customPath == null || customPath.isBlank()
            ? PathProvider.defaultPath(FILE_NAME, LINUX_STATE_DIR, osName, env)
            : Path.of(customPath);
  }

  @Override
  public Path get() {
    return path;
  }
}
