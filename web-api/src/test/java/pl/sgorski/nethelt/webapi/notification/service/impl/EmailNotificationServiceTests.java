package pl.sgorski.nethelt.webapi.notification.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.notification.domain.Notification;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationChannel;
import pl.sgorski.nethelt.webapi.notification.infrastructure.mail.EmailSender;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;

@ExtendWith(MockitoExtension.class)
public class EmailNotificationServiceTests {

  @Mock private EmailSender emailSender;
  @InjectMocks private EmailNotificationSender emailNotificationSender;

  @Test
  void supports_shouldSupportEmailChannel() {
    var result = emailNotificationSender.supports(NotificationChannel.EMAIL);

    assertTrue(result);
  }

  @Test
  void supports_shouldNotSupportWebsocketChannel() {
    var result = emailNotificationSender.supports(NotificationChannel.WEBSOCKET);

    assertFalse(result);
  }

  @Test
  void send_shouldSendWithCorrectData() {
    var user = TestUserFactory.createLocalUser();
    var notification = new Notification(user, "Test Title", "Test Content");

    emailNotificationSender.send(notification);

    verify(emailSender).send(user.getEmail(), "Test Title", "Test Content");
  }
}
