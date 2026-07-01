package pl.sgorski.nethelt.webapi.exception.domain;

import pl.sgorski.nethelt.webapi.exception.application.NotFoundException;

public final class IdentityNotFoundException extends NotFoundException {
  public IdentityNotFoundException(String message) {
    super(message);
  }
}
