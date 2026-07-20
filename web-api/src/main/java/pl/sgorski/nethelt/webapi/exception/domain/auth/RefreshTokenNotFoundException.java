package pl.sgorski.nethelt.webapi.exception.domain.auth;

import pl.sgorski.nethelt.webapi.exception.application.NotFoundException;

public final class RefreshTokenNotFoundException extends NotFoundException {
  public RefreshTokenNotFoundException() {
    super("Invalid or expired refresh token");
  }
}
