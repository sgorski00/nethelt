package pl.sgorski.nethelt.webapi.exception.domain.network;

import pl.sgorski.nethelt.webapi.exception.application.ValidationFailedException;

public class NetworkValidationFailedException extends ValidationFailedException {
  public NetworkValidationFailedException(String cause) {
    super("Network validation failed: " + cause);
  }
}
