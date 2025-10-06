package pl.sgorski.nethelt.core.exception;

public class SerializationException extends RuntimeException {

  public SerializationException(String message, Throwable cause) {
    super(message, cause);
  }

  public SerializationException(String message) {
    super(message);
  }
}
