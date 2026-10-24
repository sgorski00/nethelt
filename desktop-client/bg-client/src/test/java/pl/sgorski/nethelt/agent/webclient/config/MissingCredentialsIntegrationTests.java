package pl.sgorski.nethelt.agent.webclient.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import mockwebserver3.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import pl.sgorski.nethelt.agent.exception.MissingCredentialsException;
import pl.sgorski.nethelt.agent.security.storage.CredentialsStore;
import pl.sgorski.nethelt.agent.webclient.api.web.AgentClient;
import pl.sgorski.nethelt.agent.webclient.security.TokenProvider;

@SpringBootTest(classes = MissingCredentialsIntegrationTests.TestConfig.class)
public class MissingCredentialsIntegrationTests {

  private static final MockWebServer server = startServer();

  @Autowired private AgentClient agentClient;

  @SpringBootConfiguration
  @EnableAutoConfiguration
  @Import({
    HttpClientConfig.class,
    AuthorizationInterceptor.class,
    WebApiResponseErrorHandler.class,
    TokenProvider.class
  })
  static class TestConfig {
    @Bean
    CredentialsStore credentialsStore() {
      return new CredentialsStore() {
        @Override
        public void save(String secret) {}

        @Override
        public Optional<String> get() {
          return Optional.empty();
        }

        @Override
        public void delete() {}
      };
    }
  }

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.http.serviceclient.web-api.base-url", () -> server.url("/").toString());
  }

  @AfterAll
  static void tearDown() {
    server.close();
  }

  @Test
  void request_shouldFailWithMissingCredentials_andNotReachServer_whenPatIsNotRegistered() {
    assertThrows(MissingCredentialsException.class, () -> agentClient.heartbeat());
    assertEquals(0, server.getRequestCount());
  }

  private static MockWebServer startServer() {
    try {
      var mockWebServer = new MockWebServer();
      mockWebServer.start();
      return mockWebServer;
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
