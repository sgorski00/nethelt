package pl.sgorski.nethelt.webapi.notification.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.webapi.utils.TestNotificationFactory;

public class NotificationDeliveryTests {

  private static final Duration BACKOFF = Duration.ofSeconds(30);

  @Test
  void constructor_shouldCreatePendingDelivery() {
    var notification = TestNotificationFactory.createNotification();

    var result = new NotificationDelivery(notification, NotificationChannel.EMAIL);

    assertSame(notification, result.getNotification());
    assertEquals(NotificationChannel.EMAIL, result.getChannel());
    assertEquals(NotificationDeliveryStatus.PENDING, result.getStatus());
    assertEquals(0, result.getAttempts());
  }

  @Test
  void markSent_shouldSetSentStatusAndClearRetryData() {
    var delivery = createDelivery();
    delivery.markFailed("error", 5, BACKOFF);

    delivery.markSent();

    assertEquals(NotificationDeliveryStatus.SENT, delivery.getStatus());
    assertEquals(2, delivery.getAttempts());
    assertNotNull(delivery.getDeliveredAt());
    assertNotNull(delivery.getLastAttemptAt());
    assertNull(delivery.getNextAttemptAt());
    assertNull(delivery.getLastError());
  }

  @Test
  void markFailed_shouldSetFailedStatusAndScheduleNextAttempt() {
    var delivery = createDelivery();
    var before = Instant.now();

    delivery.markFailed("error", 5, BACKOFF);

    assertEquals(NotificationDeliveryStatus.FAILED, delivery.getStatus());
    assertEquals(1, delivery.getAttempts());
    assertEquals("error", delivery.getLastError());
    assertNotNull(delivery.getLastAttemptAt());
    assertNotNull(delivery.getNextAttemptAt());
    assertFalse(delivery.getNextAttemptAt().isBefore(before.plus(BACKOFF)));
  }

  @Test
  void markFailed_shouldDoubleBackoff_withEveryAttempt() {
    var delivery = createDelivery();
    delivery.markFailed("error", 5, BACKOFF);
    delivery.markFailed("error", 5, BACKOFF);

    delivery.markFailed("error", 5, BACKOFF);

    var delay = Duration.between(delivery.getLastAttemptAt(), delivery.getNextAttemptAt());
    assertEquals(BACKOFF.multipliedBy(4), delay);
  }

  @Test
  void markFailed_shouldSetExhaustedStatus_whenMaxAttemptsReached() {
    var delivery = createDelivery();
    delivery.markFailed("error", 2, BACKOFF);

    delivery.markFailed("last error", 2, BACKOFF);

    assertEquals(NotificationDeliveryStatus.EXHAUSTED, delivery.getStatus());
    assertEquals(2, delivery.getAttempts());
    assertEquals("last error", delivery.getLastError());
    assertNull(delivery.getNextAttemptAt());
  }

  private NotificationDelivery createDelivery() {
    return new NotificationDelivery(
        TestNotificationFactory.createNotification(), NotificationChannel.EMAIL);
  }
}
