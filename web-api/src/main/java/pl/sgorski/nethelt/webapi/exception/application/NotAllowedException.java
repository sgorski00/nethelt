package pl.sgorski.nethelt.webapi.exception.application;

public class NotAllowedException extends RuntimeException {
  public NotAllowedException(String message) {
    super(message);
  }
}
