package pl.sgorski.nethelt.agent.buffer;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class ResultBufferPathProviderTests {

  @Test
  void get_shouldReturnCustomPath_whenConfigured() {
    var provider = new ResultBufferPathProvider("/tmp/nethelt/buffer.jsonl", "Linux", _ -> null);

    assertEquals(Path.of("/tmp/nethelt/buffer.jsonl"), provider.get());
  }

  @Test
  void get_shouldReturnDefaultPath_whenCustomPathIsNull() {
    var provider = new ResultBufferPathProvider(null, "Linux", _ -> null);

    assertEquals(Path.of("/var/lib/nethelt/result-buffer.jsonl"), provider.get());
  }

  @Test
  void get_shouldReturnStateDirectoryPath_onLinux() {
    var provider = new ResultBufferPathProvider("", "Linux", _ -> null);

    assertEquals(Path.of("/var/lib/nethelt/result-buffer.jsonl"), provider.get());
  }

  @Test
  void get_shouldReturnApplicationSupportPath_onMac() {
    var provider = new ResultBufferPathProvider("", "Mac OS X", _ -> null);

    assertEquals(
        Path.of("/Library/Application Support/NetHelt/result-buffer.jsonl"), provider.get());
  }

  @Test
  void get_shouldReturnProgramDataPath_onWindows() {
    var env = Map.of("ProgramData", "D:\\ProgramData");
    var provider = new ResultBufferPathProvider("", "Windows 11", env::get);

    assertEquals(Path.of("D:\\ProgramData", "NetHelt", "result-buffer.jsonl"), provider.get());
  }

  @Test
  void get_shouldFallbackToDefaultProgramData_onWindows_whenEnvIsMissing() {
    var provider = new ResultBufferPathProvider("", "Windows 11", _ -> null);

    assertEquals(Path.of("C:\\ProgramData", "NetHelt", "result-buffer.jsonl"), provider.get());
  }
}
