package pl.sgorski.nethelt.agent.webclient.config;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpResponse;
import pl.sgorski.nethelt.agent.exception.WebClientException;

public class WebApiResponseErrorHandlerTests {

  private WebApiResponseErrorHandler responseErrorHandler;

  @BeforeEach
  void setUp() {
    responseErrorHandler = new WebApiResponseErrorHandler();
  }

  @ParameterizedTest
  @EnumSource(
      value = HttpStatus.class,
      names = {"BAD_REQUEST", "UNAUTHORIZED", "NOT_FOUND", "INTERNAL_SERVER_ERROR", "BAD_GATEWAY"})
  void hasError_shouldReturnTrue_whenStatusIs4xxOr5xx(HttpStatus status) throws Exception {
    assertTrue(responseErrorHandler.hasError(new MockClientHttpResponse(new byte[0], status)));
  }

  @ParameterizedTest
  @EnumSource(
      value = HttpStatus.class,
      names = {"OK", "CREATED", "NO_CONTENT", "FOUND"})
  void hasError_shouldReturnFalse_whenStatusIsNot4xxOr5xx(HttpStatus status) throws Exception {
    assertFalse(responseErrorHandler.hasError(new MockClientHttpResponse(new byte[0], status)));
  }

  @Test
  void handleError_shouldThrowWebClientException() {
    var response = new MockClientHttpResponse(new byte[0], HttpStatus.NOT_FOUND);
    var url = URI.create("http://localhost:8080/api/client/monitoring-tasks");

    var exception =
        assertThrows(
            WebClientException.class,
            () -> responseErrorHandler.handleError(url, HttpMethod.GET, response));

    assertEquals("Web API request failed with status 404", exception.getMessage());
  }
}
