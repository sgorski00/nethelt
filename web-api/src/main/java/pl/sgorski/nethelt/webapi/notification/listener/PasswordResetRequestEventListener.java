package pl.sgorski.nethelt.webapi.notification.listener;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.sgorski.nethelt.webapi.features.auth.dto.event.PasswordResetRequestEvent;
import pl.sgorski.nethelt.webapi.notification.domain.Notification;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationChannel;
import pl.sgorski.nethelt.webapi.notification.dto.command.NotificationCommand;
import pl.sgorski.nethelt.webapi.notification.service.NotificationDeliveryService;
import pl.sgorski.nethelt.webapi.notification.service.NotificationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordResetRequestEventListener {

  private final NotificationService notificationService;
  private final NotificationDeliveryService notificationDeliveryService;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(PasswordResetRequestEvent event) {
    try {
      var notification = createNotification(event);
      notificationDeliveryService.send(notification, Set.of(NotificationChannel.EMAIL));
    } catch (Exception e) {
      log.error(
          "Failed to send password reset notification for user with ID {}: {}",
          event.userId(),
          e.getMessage(),
          e);
    }
  }

  private Notification createNotification(PasswordResetRequestEvent event) {
    var content =
        String.format(
            """
                        Hi!
                        We received a request to reset your password.
                        If you did not make this request, please ignore this email. Otherwise, you can reset your password using the following link: %s
                        """,
            event.resetLink());
    var command =
        new NotificationCommand(event.userId(), "[Nethelt] Password Reset Request", content);
    return notificationService.create(command);
  }
}
