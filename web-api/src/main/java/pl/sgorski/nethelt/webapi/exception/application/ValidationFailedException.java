package pl.sgorski.nethelt.webapi.exception.application;

public class ValidationFailedException extends RuntimeException {
  public ValidationFailedException(String message) {
    super(message);
  }
}
