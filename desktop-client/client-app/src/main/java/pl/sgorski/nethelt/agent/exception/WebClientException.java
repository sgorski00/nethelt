package pl.sgorski.nethelt.agent.exception;

public class WebClientException extends RuntimeException {
  public WebClientException(String message, Throwable cause) {
    super(message, cause);
  }
}
