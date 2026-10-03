package pl.sgorski.nethelt.agent.executor.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import okhttp3.Request;
import org.junit.jupiter.api.Test;

public class OkHttpConfigTests {

  @Test
  void healthcheckHttpClient_shouldSendAgentUserAgent() throws Exception {
    try (var server = new MockWebServer()) {
      server.start(InetAddress.getLoopbackAddress(), 0);
      server.enqueue(new MockResponse.Builder().code(200).build());
      var client = new OkHttpConfig().healthcheckHttpClient();

      try (var _ = client.newCall(new Request.Builder().url(server.url("/")).build()).execute()) {
        var request = server.takeRequest(1, TimeUnit.SECONDS);
        assertEquals(OkHttpConfig.USER_AGENT, request.getHeaders().get("User-Agent"));
        assertTrue(OkHttpConfig.USER_AGENT.startsWith("NetHelt-Agent/"));
      }
    }
  }
}
