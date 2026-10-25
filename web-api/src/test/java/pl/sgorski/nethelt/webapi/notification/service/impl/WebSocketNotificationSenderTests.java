package pl.sgorski.nethelt.webapi.notification.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.sgorski.nethelt.webapi.exception.notification.NotificationDeliverySkippedException;
import pl.sgorski.nethelt.webapi.notification.domain.Notification;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationChannel;
import pl.sgorski.nethelt.webapi.notification.dto.response.NotificationResponse;
import pl.sgorski.nethelt.webapi.notification.mapper.NotificationMapper;
import pl.sgorski.nethelt.webapi.utils.TestNotificationFactory;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;
import pl.sgorski.nethelt.webapi.web.websocket.UserMessageSender;

@ExtendWith(MockitoExtension.class)
public class WebSocketNotificationSenderTests {

  private final NotificationResponse response =
      new NotificationResponse(1L, "Title", "Content", Instant.now(), null);

  @Mock private UserMessageSender userMessageSender;
  @Mock private NotificationMapper notificationMapper;
  @InjectMocks private WebSocketNotificationSender webSocketNotificationSender;

  @Test
  void supports_shouldSupportWebsocketChannel() {
    var result = webSocketNotificationSender.supports(NotificationChannel.WEBSOCKET);

    assertTrue(result);
  }

  @Test
  void supports_shouldNotSupportEmailChannel() {
    var result = webSocketNotificationSender.supports(NotificationChannel.EMAIL);

    assertFalse(result);
  }

  @Test
  void send_shouldPushNotificationToUser() {
    var notification = createNotification();
    when(notificationMapper.toResponse(notification)).thenReturn(response);
    when(userMessageSender.send(7L, WebSocketNotificationSender.DESTINATION, response))
        .thenReturn(true);

    webSocketNotificationSender.send(notification);

    verify(userMessageSender).send(7L, WebSocketNotificationSender.DESTINATION, response);
  }

  @Test
  void send_shouldSkip_whenUserIsNotConnected() {
    var notification = createNotification();
    when(notificationMapper.toResponse(notification)).thenReturn(response);
    when(userMessageSender.send(7L, WebSocketNotificationSender.DESTINATION, response))
        .thenReturn(false);

    assertThrows(
        NotificationDeliverySkippedException.class,
        () -> webSocketNotificationSender.send(notification));
  }

  private Notification createNotification() {
    var user = TestUserFactory.createLocalUser();
    ReflectionTestUtils.setField(user, "id", 7L);
    return TestNotificationFactory.createNotification(user);
  }
}
