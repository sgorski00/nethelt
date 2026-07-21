package pl.sgorski.nethelt.webapi.exception.domain.auth;

import pl.sgorski.nethelt.webapi.exception.application.NotFoundException;

public final class PasswordResetTokenNotFoundException extends NotFoundException {
  public PasswordResetTokenNotFoundException() {
    super("Invalid or expired password reset token");
  }
}
