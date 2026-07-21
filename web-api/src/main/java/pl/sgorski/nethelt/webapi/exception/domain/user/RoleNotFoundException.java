package pl.sgorski.nethelt.webapi.exception.domain.user;

import pl.sgorski.nethelt.webapi.exception.application.NotFoundException;

public final class RoleNotFoundException extends NotFoundException {
  public RoleNotFoundException(String message) {
    super(message);
  }
}
