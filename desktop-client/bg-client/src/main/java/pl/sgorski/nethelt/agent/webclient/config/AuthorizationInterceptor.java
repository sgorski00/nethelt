package pl.sgorski.nethelt.agent.webclient.config;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.webclient.security.TokenProvider;

@Slf4j
@Component
@RequiredArgsConstructor
public final class AuthorizationInterceptor implements ClientHttpRequestInterceptor {

  private static final String AUTHORIZATION_HEADER_PREFIX = "Agent ";

  private final ObjectProvider<TokenProvider> tokenProvider;

  @Override
  public @NonNull ClientHttpResponse intercept(
      @NonNull HttpRequest request,
      byte @NonNull [] body,
      @NonNull ClientHttpRequestExecution execution)
      throws IOException {
    if (request.getURI().getPath().contains("/authenticate")) {
      return execution.execute(request, body);
    }

    log.debug("Authenticating new HTTP request with PAT");
    tokenProvider
        .getObject()
        .getToken()
        .ifPresent(
            token ->
                request
                    .getHeaders()
                    .set(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER_PREFIX + token));
    return execution.execute(request, body);
  }
}
