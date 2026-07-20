package pl.sgorski.nethelt.webapi.exception.domain.auth;

import pl.sgorski.nethelt.webapi.exception.application.AlreadyExistsException;

public final class UserAlreadyExistsException extends AlreadyExistsException {
  public UserAlreadyExistsException() {
    super("User with passed identifier already exists");
  }
}
