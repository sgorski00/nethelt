package pl.sgorski.nethelt.agent.webclient.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.mock.http.client.MockClientHttpRequest;
import pl.sgorski.nethelt.agent.webclient.security.TokenProvider;

@ExtendWith(MockitoExtension.class)
public class AuthorizationInterceptorTests {

  private static final byte[] BODY = new byte[0];

  @Mock private ObjectProvider<TokenProvider> tokenProviderObjectProvider;
  @Mock private TokenProvider tokenProvider;
  @Mock private ClientHttpRequestExecution execution;
  @InjectMocks private AuthorizationInterceptor authorizationInterceptor;

  @Test
  void intercept_shouldAddAuthorizationHeader_whenTokenIsAvailable() throws Exception {
    var request = createRequest("/api/client/monitoring-tasks");
    when(tokenProviderObjectProvider.getObject()).thenReturn(tokenProvider);
    when(tokenProvider.getToken()).thenReturn(Optional.of("agent-token"));

    authorizationInterceptor.intercept(request, BODY, execution);

    assertEquals("Agent agent-token", request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
    verify(execution).execute(request, BODY);
  }

  @Test
  void intercept_shouldNotAddAuthorizationHeader_whenTokenIsNotAvailable() throws Exception {
    var request = createRequest("/api/client/monitoring-tasks");
    when(tokenProviderObjectProvider.getObject()).thenReturn(tokenProvider);
    when(tokenProvider.getToken()).thenReturn(Optional.empty());

    authorizationInterceptor.intercept(request, BODY, execution);

    assertFalse(request.getHeaders().containsHeader(HttpHeaders.AUTHORIZATION));
    verify(execution).execute(request, BODY);
  }

  @Test
  void intercept_shouldSkipAuthorization_whenRequestIsAuthentication() throws Exception {
    var request = createRequest("/api/client/agent/authenticate");

    authorizationInterceptor.intercept(request, BODY, execution);

    assertFalse(request.getHeaders().containsHeader(HttpHeaders.AUTHORIZATION));
    verifyNoInteractions(tokenProviderObjectProvider);
    verify(execution).execute(request, BODY);
  }

  private static MockClientHttpRequest createRequest(String path) {
    return new MockClientHttpRequest(HttpMethod.GET, URI.create("http://localhost:8080" + path));
  }
}
