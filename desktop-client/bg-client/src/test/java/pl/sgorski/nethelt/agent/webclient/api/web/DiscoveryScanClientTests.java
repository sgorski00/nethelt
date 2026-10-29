package pl.sgorski.nethelt.agent.webclient.api.web;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Optional;
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
import pl.sgorski.nethelt.agent.webclient.dto.request.DiscoveryScanAckRequest;
import pl.sgorski.nethelt.agent.webclient.dto.request.DiscoveryScanFailureRequest;
import pl.sgorski.nethelt.agent.webclient.dto.request.DiscoveryScanResultRequest;
import pl.sgorski.nethelt.agent.webclient.dto.request.DiscoveryScanResultsRequest;
import pl.sgorski.nethelt.agent.webclient.dto.response.DiscoveryScanResponse;

@SpringBootTest(classes = DiscoveryScanClientTests.TestConfig.class)
public class DiscoveryScanClientTests {

  private static final MockWebServer server = startServer();

  @Autowired private DiscoveryScanClient discoveryScanClient;

  @SpringBootConfiguration
  @EnableAutoConfiguration
  @ImportHttpServices(group = "web-api", types = DiscoveryScanClient.class)
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
  void fetchPendingScan_shouldReturnScan_whenPending() throws Exception {
    server.enqueue(
        new MockResponse.Builder()
            .code(200)
            .addHeader("Content-Type", "application/json")
            .body("{\"rangeStart\":\"192.168.0.1\",\"rangeEnd\":\"192.168.0.254\"}")
            .build());

    var result = discoveryScanClient.fetchPendingScan();

    assertEquals(Optional.of(new DiscoveryScanResponse("192.168.0.1", "192.168.0.254")), result);
    assertEquals("/client/discovery-scan/pending", server.takeRequest().getTarget());
  }

  @Test
  void fetchPendingScan_shouldReturnEmpty_whenNoContent() throws Exception {
    server.enqueue(new MockResponse.Builder().code(204).build());

    assertTrue(discoveryScanClient.fetchPendingScan().isEmpty());
    server.takeRequest();
  }

  @Test
  void acknowledgeScan_shouldSendRange() throws Exception {
    server.enqueue(new MockResponse.Builder().code(204).build());

    discoveryScanClient.acknowledgeScan(new DiscoveryScanAckRequest("10.0.0.1", "10.0.0.254"));

    var request = server.takeRequest();
    assertEquals("/client/discovery-scan/ack", request.getTarget());
    assertEquals(
        "{\"rangeStart\":\"10.0.0.1\",\"rangeEnd\":\"10.0.0.254\"}", request.getBody().utf8());
  }

  @Test
  void sendResults_shouldSendResults() throws Exception {
    server.enqueue(new MockResponse.Builder().code(204).build());

    discoveryScanClient.sendResults(
        new DiscoveryScanResultsRequest(
            List.of(new DiscoveryScanResultRequest("10.0.0.1", "router.lan", 3))));

    var request = server.takeRequest();
    assertEquals("/client/discovery-scan/results", request.getTarget());
    assertEquals(
        "{\"results\":[{\"ipAddress\":\"10.0.0.1\",\"hostname\":\"router.lan\",\"responseTimeMs\":3}]}",
        request.getBody().utf8());
  }

  @Test
  void reportFailure_shouldSendReason() throws Exception {
    server.enqueue(new MockResponse.Builder().code(204).build());

    discoveryScanClient.reportFailure(new DiscoveryScanFailureRequest("No network interface"));

    var request = server.takeRequest();
    assertEquals("/client/discovery-scan/failure", request.getTarget());
    assertEquals("{\"reason\":\"No network interface\"}", request.getBody().utf8());
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
