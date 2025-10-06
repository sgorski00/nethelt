package pl.sgorski.nethelt.core.exception;

public class NetworkException extends RuntimeException {
  public NetworkException(String message, Throwable cause) {
    super(message, cause);
  }
}
