package pl.sgorski.nethelt.agent.exception;

import org.springframework.http.HttpStatusCode;

public final class WebClientException extends RuntimeException {

  private final HttpStatusCode status;

  public WebClientException(HttpStatusCode status) {
    this("Web API request failed with status " + status.value(), status);
  }

  public WebClientException(String message, HttpStatusCode status) {
    super(message);
    this.status = status;
  }

  public HttpStatusCode getStatus() {
    return status;
  }
}
