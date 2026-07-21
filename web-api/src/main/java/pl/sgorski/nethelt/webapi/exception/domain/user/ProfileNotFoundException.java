package pl.sgorski.nethelt.webapi.exception.domain.user;

import pl.sgorski.nethelt.webapi.exception.application.NotFoundException;

public final class ProfileNotFoundException extends NotFoundException {
  public ProfileNotFoundException(String message) {
    super(message);
  }
}
