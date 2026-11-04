package pl.sgorski.nethelt.agent.buffer.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.buffer.ResultBuffer;
import pl.sgorski.nethelt.agent.buffer.ResultBufferPathProvider;
import pl.sgorski.nethelt.agent.buffer.ResultBufferProperties;
import pl.sgorski.nethelt.agent.webclient.dto.request.MonitoringResultRequest;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
public final class JsonFileResultBuffer implements ResultBuffer {

  private final Path file;
  private final int maxSize;
  private final int compactionThreshold;
  private final JsonMapper jsonMapper;

  private final Deque<MonitoringResultRequest> results = new ArrayDeque<>();
  private int fileLines;
  private boolean writeFailed;

  public JsonFileResultBuffer(
      ResultBufferPathProvider pathProvider,
      ResultBufferProperties properties,
      JsonMapper jsonMapper) {
    this.file = pathProvider.get().toAbsolutePath();
    this.maxSize = properties.maxSize();
    this.compactionThreshold = maxSize + Math.max(1, maxSize / 10);
    this.jsonMapper = jsonMapper;
    load();
  }

  @Override
  public synchronized void add(MonitoringResultRequest request) {
    results.addLast(request);
    dropOldest();
    if (fileLines + 1 > compactionThreshold) {
      rewrite();
      return;
    }
    append(request);
  }

  @Override
  public synchronized List<MonitoringResultRequest> peek(int limit) {
    return results.stream().limit(limit).toList();
  }

  @Override
  public synchronized void remove(Collection<MonitoringResultRequest> requests) {
    if (requests.isEmpty()) return;

    var toRemove =
        Collections.newSetFromMap(new IdentityHashMap<MonitoringResultRequest, Boolean>());
    toRemove.addAll(requests);
    if (results.removeIf(toRemove::contains)) {
      rewrite();
    }
  }

  @Override
  public synchronized int size() {
    return results.size();
  }

  private void load() {
    List<String> lines;
    try {
      lines = Files.readAllLines(file, StandardCharsets.UTF_8);
    } catch (NoSuchFileException e) {
      return;
    } catch (IOException e) {
      log.error("Cannot read result buffer from {}, starting empty: {}", file, e.getMessage());
      return;
    }

    var corrupted = 0;
    for (var line : lines) {
      if (line.isBlank()) continue;
      try {
        results.addLast(jsonMapper.readValue(line, MonitoringResultRequest.class));
      } catch (JacksonException e) {
        corrupted++;
      }
    }
    if (corrupted > 0) {
      log.warn("Skipped {} corrupted line(s) of result buffer {}", corrupted, file);
    }
    var dropped = dropOldest();
    fileLines = lines.size();
    log.info("Loaded {} buffered result(s) from {}", results.size(), file);

    // A corrupted last line has no line break, the next append would join it with a new result
    if (corrupted > 0 || dropped > 0) {
      rewrite();
    }
  }

  private int dropOldest() {
    var dropped = 0;
    while (results.size() > maxSize) {
      results.removeFirst();
      dropped++;
    }
    if (dropped > 0) {
      log.warn("Result buffer is full ({}), dropped {} oldest result(s)", maxSize, dropped);
    }
    return dropped;
  }

  private void append(MonitoringResultRequest request) {
    try {
      createDirectory();
      Files.writeString(
          file,
          toLine(request),
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE,
          StandardOpenOption.APPEND);
      fileLines++;
      logWriteRecovery();
    } catch (IOException e) {
      logFirstWriteFailure(e);
    }
  }

  private void rewrite() {
    Path tempFile = null;
    try {
      if (results.isEmpty()) {
        Files.deleteIfExists(file);
      } else {
        var directory = createDirectory();
        tempFile = Files.createTempFile(directory, ".result-buffer", ".tmp");
        try (var writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
          for (var request : results) {
            writer.write(toLine(request));
          }
        }
        Files.move(
            tempFile, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      }
      fileLines = results.size();
      logWriteRecovery();
    } catch (IOException e) {
      logFirstWriteFailure(e);
    } finally {
      deleteIfExists(tempFile);
    }
  }

  private Path createDirectory() throws IOException {
    return Files.createDirectories(file.getParent());
  }

  private String toLine(MonitoringResultRequest request) {
    return jsonMapper.writeValueAsString(request) + "\n";
  }

  private void logFirstWriteFailure(IOException e) {
    if (!writeFailed) {
      log.error(
          "Cannot write result buffer to {}, buffered results will be lost on restart: {}",
          file,
          e.toString());
      writeFailed = true;
    }
  }

  private void logWriteRecovery() {
    if (writeFailed) {
      log.info("Result buffer {} is writable again", file);
      writeFailed = false;
    }
  }

  private static void deleteIfExists(Path file) {
    if (file == null) return;

    try {
      Files.deleteIfExists(file);
    } catch (IOException ignored) {
    }
  }
}
