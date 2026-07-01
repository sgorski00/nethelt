package pl.sgorski.nethelt.webapi.exception.domain;

import pl.sgorski.nethelt.webapi.exception.application.NotFoundException;

public final class UserNotFoundException extends NotFoundException {
  public UserNotFoundException(String message) {
    super(message);
  }
}
