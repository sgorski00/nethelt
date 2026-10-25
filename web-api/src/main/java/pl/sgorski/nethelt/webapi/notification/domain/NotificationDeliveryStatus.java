package pl.sgorski.nethelt.webapi.notification.domain;

public enum NotificationDeliveryStatus {
  PENDING,
  SENT,
  FAILED,
  EXHAUSTED,
  SKIPPED
}
