package pl.sgorski.nethelt.agent.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;
import pl.sgorski.nethelt.agent.buffer.ResultBuffer;
import pl.sgorski.nethelt.agent.buffer.ResultBufferPathProvider;
import pl.sgorski.nethelt.agent.buffer.ResultBufferProperties;
import pl.sgorski.nethelt.agent.buffer.impl.JsonFileResultBuffer;
import pl.sgorski.nethelt.agent.webclient.api.web.MonitoringResultClient;
import pl.sgorski.nethelt.agent.webclient.config.WebApiResponseErrorHandler;
import pl.sgorski.nethelt.agent.webclient.dto.request.MonitoringResultRequest;
import pl.sgorski.nethelt.agent.webclient.dto.request.TelnetResultExtensionRequest;

@SpringBootTest(classes = MonitoringResultBufferingIntegrationTests.TestConfig.class)
public class MonitoringResultBufferingIntegrationTests {

  private static final MockWebServer server = startServer();
  private static final Path bufferFile = createTempDir().resolve("result-buffer.jsonl");

  @Autowired private MonitoringResultSender sender;
  @Autowired private ResultBuffer resultBuffer;

  @SpringBootConfiguration
  @EnableAutoConfiguration
  @EnableConfigurationProperties(ResultBufferProperties.class)
  @ImportHttpServices(group = "web-api", types = MonitoringResultClient.class)
  @Import({
    WebApiResponseErrorHandler.class,
    ResultBufferPathProvider.class,
    JsonFileResultBuffer.class,
    MonitoringResultSender.class
  })
  static class TestConfig {
    @Bean
    RestClientHttpServiceGroupConfigurer testServiceGroupConfigurer(
        WebApiResponseErrorHandler responseErrorHandler) {
      return groups ->
          groups.forEachClient(
              (_, clientBuilder) -> clientBuilder.defaultStatusHandler(responseErrorHandler));
    }
  }

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.http.serviceclient.web-api.base-url", () -> server.url("/").toString());
    registry.add("buffer.path", bufferFile::toString);
  }

  @AfterAll
  static void tearDown() throws IOException {
    server.close();
    Files.deleteIfExists(bufferFile);
    Files.deleteIfExists(bufferFile.getParent());
  }

  @Test
  void result_shouldBeBufferedWhileApiFails_andSentLaterWithOriginalExecutedAt() throws Exception {
    var request =
        new MonitoringResultRequest(
            7L,
            Instant.parse("2026-11-04T12:00:00.5Z"),
            true,
            "OK",
            12L,
            new TelnetResultExtensionRequest(true));
    server.enqueue(new MockResponse.Builder().code(503).build());

    sender.send(request);

    server.takeRequest();
    assertEquals(1, resultBuffer.size());
    assertTrue(
        Files.readString(bufferFile).contains("\"executedAt\":\"2026-11-04T12:00:00.500Z\""));

    server.enqueue(new MockResponse.Builder().code(201).build());

    sender.flushBuffer();

    var body = server.takeRequest().getBody().utf8();
    assertEquals(
        "{\"taskId\":7,\"executedAt\":\"2026-11-04T12:00:00.500Z\",\"success\":true,"
            + "\"message\":\"OK\",\"responseTimeMs\":12,"
            + "\"extension\":{\"type\":\"TELNET\",\"portOpen\":true}}",
        body);
    assertEquals(0, resultBuffer.size());
    assertFalse(Files.exists(bufferFile));
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

  private static Path createTempDir() {
    try {
      return Files.createTempDirectory("nethelt-buffer");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
