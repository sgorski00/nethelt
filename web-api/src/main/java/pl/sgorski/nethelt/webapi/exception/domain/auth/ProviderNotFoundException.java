package pl.sgorski.nethelt.webapi.exception.domain.auth;

import pl.sgorski.nethelt.webapi.exception.application.NotFoundException;

public final class ProviderNotFoundException extends NotFoundException {
  public ProviderNotFoundException(String message) {
    super(message);
  }
}
