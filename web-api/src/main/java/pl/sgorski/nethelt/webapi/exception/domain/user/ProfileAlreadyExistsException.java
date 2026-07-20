package pl.sgorski.nethelt.webapi.exception.domain.user;

import pl.sgorski.nethelt.webapi.exception.application.AlreadyExistsException;

public final class ProfileAlreadyExistsException extends AlreadyExistsException {
  public ProfileAlreadyExistsException() {
    super("Couldn't save profile because it is already created.");
  }
}
