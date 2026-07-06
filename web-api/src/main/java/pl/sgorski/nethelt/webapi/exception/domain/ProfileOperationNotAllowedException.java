package pl.sgorski.nethelt.webapi.exception.domain;

import pl.sgorski.nethelt.webapi.exception.application.NotAllowedException;

public final class ProfileOperationNotAllowedException extends NotAllowedException {
  public ProfileOperationNotAllowedException(String message) {
    super(message);
  }
}
