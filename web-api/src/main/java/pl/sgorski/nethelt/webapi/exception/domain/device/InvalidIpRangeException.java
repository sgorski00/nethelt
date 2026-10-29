package pl.sgorski.nethelt.webapi.exception.domain.device;

import pl.sgorski.nethelt.webapi.exception.application.ValidationFailedException;

public final class InvalidIpRangeException extends ValidationFailedException {
  public InvalidIpRangeException(String value, String reason) {
    super("Invalid IP range '" + value + "': " + reason);
  }
}
