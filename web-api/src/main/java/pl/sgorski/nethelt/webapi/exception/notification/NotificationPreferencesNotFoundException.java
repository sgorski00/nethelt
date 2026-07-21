package pl.sgorski.nethelt.webapi.exception.notification;

import pl.sgorski.nethelt.webapi.exception.application.NotFoundException;

public final class NotificationPreferencesNotFoundException extends NotFoundException {
  public NotificationPreferencesNotFoundException() {
    super("Notification preferences not found");
  }
}
