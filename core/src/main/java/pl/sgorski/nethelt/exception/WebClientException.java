package pl.sgorski.nethelt.exception;

public final class WebClientException extends RuntimeException {
  public WebClientException(String message, Throwable cause) {
    super(message, cause);
  }
}
