package pl.sgorski.nethelt.agent.network.http_healthcheck;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.agent.exception.NetworkException;
import pl.sgorski.nethelt.agent.model.monitoring_task.MonitoringTask;
import pl.sgorski.nethelt.agent.network.http_healthcheck.impl.DefaultHttpHealthcheckOperation;
import pl.sgorski.nethelt.agent.test_utils.TestMonitoringTaskFactory;

public class DefaultHttpHealthcheckOperationTests {

  private static final InetAddress LOOPBACK = InetAddress.getLoopbackAddress();
  private static final Duration TIMEOUT = Duration.ofSeconds(2);

  private MockWebServer server;
  private DefaultHttpHealthcheckOperation healthcheckOperation;

  @BeforeEach
  void setUp() throws Exception {
    server = new MockWebServer();
    server.start(LOOPBACK, 0);
    healthcheckOperation = new DefaultHttpHealthcheckOperation(new OkHttpClient());
  }

  @AfterEach
  void tearDown() {
    server.close();
  }

  @Test
  void execute_shouldReturnSuccess_whenResponseIs2xx() {
    server.enqueue(new MockResponse.Builder().code(200).build());
    var task = createTask("/health", null, TIMEOUT);

    var result = healthcheckOperation.execute(task);

    assertSame(1L, result.getTaskId());
    assertTrue(result.isSuccess());
    assertEquals(200, result.getStatusCode());
    assertTrue(result.getResponseTimeMs() >= 0);
  }

  @Test
  void execute_shouldReturnFailure_whenResponseIsNot2xx() {
    server.enqueue(new MockResponse.Builder().code(503).build());
    var task = createTask("/health", null, TIMEOUT);

    var result = healthcheckOperation.execute(task);

    assertFalse(result.isSuccess());
    assertEquals(503, result.getStatusCode());
    assertTrue(result.getMessage().endsWith("(expected 2xx)"));
  }

  @Test
  void execute_shouldReturnSuccess_whenResponseMatchesExpectedStatusCode() {
    server.enqueue(new MockResponse.Builder().code(403).build());
    var task = createTask("/health", null, 403);

    var result = healthcheckOperation.execute(task);

    assertTrue(result.isSuccess());
    assertEquals(403, result.getStatusCode());
  }

  @Test
  void execute_shouldReturnFailure_whenResponseIs2xxButNotExpectedStatusCode() {
    server.enqueue(new MockResponse.Builder().code(204).build());
    var task = createTask("/health", null, 200);

    var result = healthcheckOperation.execute(task);

    assertFalse(result.isSuccess());
    assertEquals(204, result.getStatusCode());
    assertTrue(result.getMessage().endsWith("(expected 200)"));
  }

  @Test
  void execute_shouldReturnFailureWithoutStatusCode_whenTimeoutIsExceeded() {
    server.enqueue(new MockResponse.Builder().code(200).headersDelay(2, TimeUnit.SECONDS).build());
    var task = createTask("/health", null, Duration.ofMillis(200));

    var result = healthcheckOperation.execute(task);

    assertFalse(result.isSuccess());
    assertNull(result.getStatusCode());
    assertTrue(result.getResponseTimeMs() >= 200);
  }

  @Test
  void execute_shouldReturnFailureWithoutStatusCode_whenConnectionIsRefused() {
    var port = server.getPort();
    server.close();
    var config =
        TestMonitoringTaskFactory.createHttpHealthcheckTaskConfiguration(
            port, "/health", null, TIMEOUT);
    var task = TestMonitoringTaskFactory.createHttpHealthcheckMonitoringTask(1L, LOOPBACK, config);

    var result = healthcheckOperation.execute(task);

    assertFalse(result.isSuccess());
    assertNull(result.getStatusCode());
  }

  @Test
  void execute_shouldRequestDeviceIp_whenHostIsNotConfigured() throws Exception {
    server.enqueue(new MockResponse.Builder().code(200).build());
    var task = createTask("/health", null, TIMEOUT);

    healthcheckOperation.execute(task);

    var request = server.takeRequest(1, TimeUnit.SECONDS);
    assertEquals("/health", request.getTarget());
    assertEquals(
        LOOPBACK.getHostAddress() + ":" + server.getPort(), request.getHeaders().get("Host"));
    assertEquals("close", request.getHeaders().get("Connection"));
  }

  @Test
  void execute_shouldSendConfiguredHostToDeviceIp_whenHostIsConfigured() throws Exception {
    server.enqueue(new MockResponse.Builder().code(200).build());
    var task = createTask("/health", "device.nethelt.test", TIMEOUT);

    var result = healthcheckOperation.execute(task);

    assertTrue(result.isSuccess());
    var request = server.takeRequest(1, TimeUnit.SECONDS);
    assertEquals("device.nethelt.test:" + server.getPort(), request.getHeaders().get("Host"));
  }

  @Test
  void execute_shouldThrowNetworkException_whenPathIsInvalid() {
    var task = createTask("health", null, TIMEOUT);

    assertThrows(NetworkException.class, () -> healthcheckOperation.execute(task));
  }

  @Test
  void error_shouldReturnErrorHttpHealthcheckResult() {
    var task = createTask("/health", null, TIMEOUT);

    var result = healthcheckOperation.error(task);

    assertSame(task.id(), result.getTaskId());
    assertFalse(result.isSuccess());
    assertNull(result.getStatusCode());
    assertEquals("HTTP Healthcheck failed", result.getMessage());
    assertEquals(-1, result.getResponseTimeMs());
  }

  private MonitoringTask createTask(String path, String host, Duration timeout) {
    var config =
        TestMonitoringTaskFactory.createHttpHealthcheckTaskConfiguration(
            server.getPort(), path, host, timeout);
    return TestMonitoringTaskFactory.createHttpHealthcheckMonitoringTask(1L, LOOPBACK, config);
  }

  private MonitoringTask createTask(String path, String host, Integer expectedStatusCode) {
    var config =
        TestMonitoringTaskFactory.createHttpHealthcheckTaskConfiguration(
            server.getPort(), path, host, expectedStatusCode, TIMEOUT);
    return TestMonitoringTaskFactory.createHttpHealthcheckMonitoringTask(1L, LOOPBACK, config);
  }
}
