package pl.sgorski.nethelt.webapi.exception.notification;

public class NotificationDeliverySkippedException extends RuntimeException {
  public NotificationDeliverySkippedException(String message) {
    super(message);
  }
}
