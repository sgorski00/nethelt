package pl.sgorski.nethelt.webapi.exception.domain.device;

import pl.sgorski.nethelt.webapi.exception.application.ValidationFailedException;

public final class DeviceValidationFailedException extends ValidationFailedException {
  public DeviceValidationFailedException(String cause) {
    super("Device validation failed: " + cause);
  }
}
