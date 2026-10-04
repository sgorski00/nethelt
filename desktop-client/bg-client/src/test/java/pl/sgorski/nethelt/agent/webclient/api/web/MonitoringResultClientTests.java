package pl.sgorski.nethelt.agent.webclient.api.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.validation.ConstraintViolationException;
import java.io.IOException;
import java.net.InetAddress;
import java.time.Instant;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.service.registry.ImportHttpServices;
import pl.sgorski.nethelt.agent.webclient.dto.request.HttpHealthcheckResultExtensionRequest;
import pl.sgorski.nethelt.agent.webclient.dto.request.MonitoringResultRequest;
import pl.sgorski.nethelt.agent.webclient.dto.request.PingResultExtensionRequest;
import pl.sgorski.nethelt.agent.webclient.dto.request.TelnetResultExtensionRequest;

@SpringBootTest(classes = MonitoringResultClientTests.TestConfig.class)
public class MonitoringResultClientTests {

  private static final MockWebServer server = startServer();

  @Autowired private MonitoringResultClient monitoringResultClient;

  @SpringBootConfiguration
  @EnableAutoConfiguration
  @ImportHttpServices(group = "web-api", types = MonitoringResultClient.class)
  static class TestConfig {}

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.http.serviceclient.web-api.base-url", () -> server.url("/").toString());
  }

  @AfterAll
  static void tearDown() {
    server.close();
  }

  @Test
  void sendMonitoringResult_shouldSendTypedExtension_whenRequestIsValid() throws Exception {
    server.enqueue(new MockResponse.Builder().code(201).build());
    var request =
        new MonitoringResultRequest(
            1L,
            Instant.parse("2026-10-04T12:00:00Z"),
            true,
            null,
            12L,
            new TelnetResultExtensionRequest(true));

    monitoringResultClient.sendMonitoringResult(request);

    var body = server.takeRequest().getBody().utf8();
    assertEquals(
        "{\"taskId\":1,\"executedAt\":\"2026-10-04T12:00:00Z\",\"success\":true,\"message\":null,"
            + "\"responseTimeMs\":12,\"extension\":{\"type\":\"TELNET\",\"portOpen\":true}}",
        body);
  }

  @Test
  void sendMonitoringResult_shouldThrow_whenTaskIdIsNull() {
    var request =
        new MonitoringResultRequest(
            null, Instant.now(), true, null, 12L, new PingResultExtensionRequest());

    assertThrows(
        ConstraintViolationException.class,
        () -> monitoringResultClient.sendMonitoringResult(request));
  }

  @Test
  void sendMonitoringResult_shouldThrow_whenResponseTimeIsNegative() {
    var request =
        new MonitoringResultRequest(
            1L, Instant.now(), true, null, -1L, new PingResultExtensionRequest());

    assertThrows(
        ConstraintViolationException.class,
        () -> monitoringResultClient.sendMonitoringResult(request));
  }

  @Test
  void sendMonitoringResult_shouldThrow_whenExtensionIsInvalid() {
    var request =
        new MonitoringResultRequest(
            1L, Instant.now(), false, null, null, new HttpHealthcheckResultExtensionRequest(42));

    assertThrows(
        ConstraintViolationException.class,
        () -> monitoringResultClient.sendMonitoringResult(request));
  }

  private static MockWebServer startServer() {
    var mockWebServer = new MockWebServer();
    try {
      mockWebServer.start(InetAddress.getLoopbackAddress(), 0);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return mockWebServer;
  }
}
