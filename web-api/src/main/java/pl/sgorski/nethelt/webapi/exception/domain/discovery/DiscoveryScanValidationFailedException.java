package pl.sgorski.nethelt.webapi.exception.domain.discovery;

import pl.sgorski.nethelt.webapi.exception.application.ValidationFailedException;

public final class DiscoveryScanValidationFailedException extends ValidationFailedException {
  public DiscoveryScanValidationFailedException(String cause) {
    super("Discovery scan validation failed: " + cause);
  }
}
