package pl.sgorski.nethelt.webapi.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.notification.config.NotificationDeliveryProperties;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationChannel;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationDelivery;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationDeliveryStatus;
import pl.sgorski.nethelt.webapi.notification.repository.NotificationDeliveryRepository;
import pl.sgorski.nethelt.webapi.utils.TestNotificationFactory;

@ExtendWith(MockitoExtension.class)
public class NotificationDeliveryServiceTests {

  private final NotificationDeliveryProperties properties =
      new NotificationDeliveryProperties(3, Duration.ofSeconds(30), 50, Duration.ofDays(30));

  @Mock private NotificationSender emailSender;
  @Mock private NotificationSender websocketSender;
  @Mock private NotificationDeliveryRepository notificationDeliveryRepository;
  private NotificationDeliveryService notificationDeliveryService;

  @BeforeEach
  void setUp() {
    this.notificationDeliveryService =
        new NotificationDeliveryService(
            List.of(emailSender, websocketSender), notificationDeliveryRepository, properties);
    lenient().when(emailSender.supports(NotificationChannel.EMAIL)).thenReturn(true);
    lenient().when(websocketSender.supports(NotificationChannel.WEBSOCKET)).thenReturn(true);
    lenient()
        .when(notificationDeliveryRepository.save(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  void send_shouldRecordSentDelivery_whenSenderSucceeds() {
    var notification = TestNotificationFactory.createNotification();

    notificationDeliveryService.send(notification, Set.of(NotificationChannel.EMAIL));

    verify(emailSender).send(notification);
    var delivery = captureSavedDelivery();
    assertSame(notification, delivery.getNotification());
    assertEquals(NotificationChannel.EMAIL, delivery.getChannel());
    assertEquals(NotificationDeliveryStatus.SENT, delivery.getStatus());
    assertEquals(1, delivery.getAttempts());
  }

  @Test
  void send_shouldRecordFailedDelivery_whenSenderThrows() {
    var notification = TestNotificationFactory.createNotification();
    doThrow(new RuntimeException("SMTP down")).when(emailSender).send(notification);

    notificationDeliveryService.send(notification, Set.of(NotificationChannel.EMAIL));

    var delivery = captureSavedDelivery();
    assertEquals(NotificationDeliveryStatus.FAILED, delivery.getStatus());
    assertEquals("SMTP down", delivery.getLastError());
    assertNotNull(delivery.getNextAttemptAt());
  }

  @Test
  void send_shouldDeliverOtherChannels_whenOneSenderThrows() {
    var notification = TestNotificationFactory.createNotification();
    doThrow(new RuntimeException("SMTP down")).when(emailSender).send(notification);

    notificationDeliveryService.send(
        notification, Set.of(NotificationChannel.EMAIL, NotificationChannel.WEBSOCKET));

    verify(websocketSender).send(notification);
  }

  @Test
  void send_shouldNotRecordDelivery_whenChannelIsNotSupported() {
    var notification = TestNotificationFactory.createNotification();
    when(emailSender.supports(NotificationChannel.EMAIL)).thenReturn(false);

    notificationDeliveryService.send(notification, Set.of(NotificationChannel.EMAIL));

    verify(emailSender, never()).send(any());
    verify(notificationDeliveryRepository, never()).save(any());
  }

  @Test
  void retryFailedDeliveries_shouldResendDueDeliveries() {
    var notification = TestNotificationFactory.createNotification();
    var delivery = new NotificationDelivery(notification, NotificationChannel.EMAIL);
    delivery.markFailed("error", properties.maxAttempts(), properties.retryBackoff());
    when(notificationDeliveryRepository.findDue(
            eq(NotificationDeliveryStatus.FAILED), any(), any()))
        .thenReturn(List.of(delivery));

    notificationDeliveryService.retryFailedDeliveries();

    verify(emailSender).send(notification);
    verify(notificationDeliveryRepository).save(delivery);
    assertEquals(NotificationDeliveryStatus.SENT, delivery.getStatus());
    assertEquals(2, delivery.getAttempts());
  }

  @Test
  void retryFailedDeliveries_shouldMarkExhausted_whenMaxAttemptsReached() {
    var notification = TestNotificationFactory.createNotification();
    var delivery = new NotificationDelivery(notification, NotificationChannel.EMAIL);
    delivery.markFailed("error", properties.maxAttempts(), properties.retryBackoff());
    delivery.markFailed("error", properties.maxAttempts(), properties.retryBackoff());
    when(notificationDeliveryRepository.findDue(
            eq(NotificationDeliveryStatus.FAILED), any(), any()))
        .thenReturn(List.of(delivery));
    doThrow(new RuntimeException("SMTP down")).when(emailSender).send(notification);

    notificationDeliveryService.retryFailedDeliveries();

    assertEquals(NotificationDeliveryStatus.EXHAUSTED, delivery.getStatus());
    assertEquals(3, delivery.getAttempts());
  }

  @Test
  void retryFailedDeliveries_shouldMarkFailed_whenChannelIsNoLongerSupported() {
    var notification = TestNotificationFactory.createNotification();
    var delivery = new NotificationDelivery(notification, NotificationChannel.EMAIL);
    when(emailSender.supports(NotificationChannel.EMAIL)).thenReturn(false);
    when(notificationDeliveryRepository.findDue(
            eq(NotificationDeliveryStatus.FAILED), any(), any()))
        .thenReturn(List.of(delivery));

    notificationDeliveryService.retryFailedDeliveries();

    verify(emailSender, never()).send(any());
    assertEquals(NotificationDeliveryStatus.FAILED, delivery.getStatus());
    assertNotNull(delivery.getLastError());
  }

  @Test
  void deleteFinishedDeliveries_shouldDeleteSentAndExhaustedDeliveries() {
    when(notificationDeliveryRepository.deleteByStatusInAndUpdatedAtBefore(any(), any()))
        .thenReturn(4);

    var result = notificationDeliveryService.deleteFinishedDeliveries();

    assertEquals(4, result);
    verify(notificationDeliveryRepository)
        .deleteByStatusInAndUpdatedAtBefore(
            eq(Set.of(NotificationDeliveryStatus.SENT, NotificationDeliveryStatus.EXHAUSTED)),
            any());
  }

  private NotificationDelivery captureSavedDelivery() {
    var captor = ArgumentCaptor.forClass(NotificationDelivery.class);
    verify(notificationDeliveryRepository, atLeastOnce()).save(captor.capture());
    return captor.getValue();
  }
}
