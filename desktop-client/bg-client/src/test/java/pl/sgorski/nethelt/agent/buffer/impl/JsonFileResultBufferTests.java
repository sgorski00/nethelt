package pl.sgorski.nethelt.agent.buffer.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pl.sgorski.nethelt.agent.buffer.ResultBufferPathProvider;
import pl.sgorski.nethelt.agent.buffer.ResultBufferProperties;
import pl.sgorski.nethelt.agent.webclient.dto.request.HttpHealthcheckResultExtensionRequest;
import pl.sgorski.nethelt.agent.webclient.dto.request.MonitoringResultExtensionRequest;
import pl.sgorski.nethelt.agent.webclient.dto.request.MonitoringResultRequest;
import pl.sgorski.nethelt.agent.webclient.dto.request.PingResultExtensionRequest;
import pl.sgorski.nethelt.agent.webclient.dto.request.TelnetResultExtensionRequest;
import tools.jackson.databind.json.JsonMapper;

public class JsonFileResultBufferTests {

  private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

  @TempDir private Path tempDir;
  private Path file;

  @BeforeEach
  void setUp() {
    file = tempDir.resolve("nethelt").resolve("result-buffer.jsonl");
  }

  @Test
  void add_shouldKeepResultsInOrder() {
    var buffer = createBuffer(10);
    var first = createRequest(1L);
    var second = createRequest(2L);

    buffer.add(first);
    buffer.add(second);

    assertEquals(List.of(first, second), buffer.peek(10));
    assertEquals(2, buffer.size());
  }

  @Test
  void add_shouldDropOldestResults_whenBufferIsFull() {
    var buffer = createBuffer(2);

    buffer.add(createRequest(1L));
    buffer.add(createRequest(2L));
    buffer.add(createRequest(3L));

    assertEquals(List.of(2L, 3L), taskIds(buffer.peek(10)));
  }

  @Test
  void add_shouldCompactFile_whenItHasTooManyDroppedResults() throws IOException {
    var buffer = createBuffer(2);

    for (long i = 1; i <= 10; i++) {
      buffer.add(createRequest(i));
    }

    assertTrue(Files.readAllLines(file).size() <= 3);
    assertEquals(List.of(9L, 10L), taskIds(createBuffer(2).peek(10)));
  }

  @Test
  void peek_shouldReturnOnlyLimitedNumberOfOldestResults() {
    var buffer = createBuffer(10);
    buffer.add(createRequest(1L));
    buffer.add(createRequest(2L));
    buffer.add(createRequest(3L));

    assertEquals(List.of(1L, 2L), taskIds(buffer.peek(2)));
    assertEquals(3, buffer.size());
  }

  @Test
  void remove_shouldRemoveOnlyGivenResults() {
    var buffer = createBuffer(10);
    buffer.add(createRequest(1L));
    buffer.add(createRequest(2L));
    buffer.add(createRequest(3L));

    buffer.remove(buffer.peek(2));

    assertEquals(List.of(3L), taskIds(buffer.peek(10)));
    assertEquals(List.of(3L), taskIds(createBuffer(10).peek(10)));
  }

  @Test
  void remove_shouldRemoveByIdentity_whenResultsAreEqual() {
    var buffer = createBuffer(10);
    buffer.add(createRequest(1L));
    buffer.add(createRequest(1L));

    buffer.remove(buffer.peek(1));

    assertEquals(1, buffer.size());
  }

  @Test
  void remove_shouldDeleteFile_whenBufferIsEmpty() {
    var buffer = createBuffer(10);
    buffer.add(createRequest(1L));

    buffer.remove(buffer.peek(10));

    assertFalse(Files.exists(file));
  }

  @Test
  void load_shouldRestoreResults_afterRestart() {
    var executedAt = Instant.parse("2026-11-04T12:00:00.123456Z");
    var ping = createRequest(1L, executedAt, new PingResultExtensionRequest());
    var telnet = createRequest(2L, executedAt, new TelnetResultExtensionRequest(true));
    var http = createRequest(3L, executedAt, new HttpHealthcheckResultExtensionRequest(503));
    var buffer = createBuffer(10);
    buffer.add(ping);
    buffer.add(telnet);
    buffer.add(http);

    var restored = createBuffer(10).peek(10);

    assertEquals(List.of(ping, telnet, http), restored);
  }

  @Test
  void load_shouldKeepNewestResults_whenFileHasMoreThanMaxSize() {
    var buffer = createBuffer(10);
    for (long i = 1; i <= 5; i++) {
      buffer.add(createRequest(i));
    }

    assertEquals(List.of(4L, 5L), taskIds(createBuffer(2).peek(10)));
  }

  @Test
  void load_shouldSkipCorruptedLines() throws IOException {
    Files.createDirectories(file.getParent());
    Files.writeString(
        file,
        JSON_MAPPER.writeValueAsString(createRequest(1L)) + "\nnot json\n{\"taskId\": 2, \"exec");
    var buffer = createBuffer(10);

    buffer.add(createRequest(3L));

    assertEquals(List.of(1L, 3L), taskIds(buffer.peek(10)));
    assertEquals(List.of(1L, 3L), taskIds(createBuffer(10).peek(10)));
  }

  @Test
  void load_shouldStartEmpty_whenFileDoesNotExist() {
    assertEquals(0, createBuffer(10).size());
  }

  @Test
  void add_shouldKeepResultsInMemory_whenFileCannotBeWritten() throws IOException {
    Files.writeString(tempDir.resolve("nethelt"), "not a directory");
    var buffer = createBuffer(10);
    var request = createRequest(1L);

    buffer.add(request);

    assertEquals(List.of(request), buffer.peek(10));
  }

  private JsonFileResultBuffer createBuffer(int maxSize) {
    var properties = new ResultBufferProperties(file.toString(), maxSize, 100);
    return new JsonFileResultBuffer(
        new ResultBufferPathProvider(properties), properties, JSON_MAPPER);
  }

  private static MonitoringResultRequest createRequest(Long taskId) {
    return createRequest(
        taskId, Instant.parse("2026-11-04T12:00:00Z"), new PingResultExtensionRequest());
  }

  private static MonitoringResultRequest createRequest(
      Long taskId, Instant executedAt, MonitoringResultExtensionRequest extension) {
    return new MonitoringResultRequest(taskId, executedAt, true, "OK", 12L, extension);
  }

  private static List<Long> taskIds(List<MonitoringResultRequest> requests) {
    return requests.stream().map(MonitoringResultRequest::taskId).toList();
  }
}
