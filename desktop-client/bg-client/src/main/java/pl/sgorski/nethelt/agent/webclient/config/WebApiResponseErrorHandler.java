package pl.sgorski.nethelt.agent.webclient.config;

import java.io.IOException;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import pl.sgorski.nethelt.agent.exception.WebClientException;

@Slf4j
@Component
public final class WebApiResponseErrorHandler implements ResponseErrorHandler {

  @Override
  public boolean hasError(ClientHttpResponse response) throws IOException {
    return response.getStatusCode().is4xxClientError()
        || response.getStatusCode().is5xxServerError();
  }

  @Override
  public void handleError(@NonNull URI url, HttpMethod method, ClientHttpResponse response)
      throws IOException {
    var status = response.getStatusCode();
    log.warn("Web API request failed: {} {} -> {}", method.name(), url, status);
    throw new WebClientException("Web API request failed with status " + status.value());
  }
}
