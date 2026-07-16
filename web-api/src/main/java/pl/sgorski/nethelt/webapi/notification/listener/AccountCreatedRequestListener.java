package pl.sgorski.nethelt.webapi.notification.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.sgorski.nethelt.webapi.features.auth.dto.event.AccountCreatedEvent;
import pl.sgorski.nethelt.webapi.notification.domain.Notification;
import pl.sgorski.nethelt.webapi.notification.dto.command.NotificationCommand;
import pl.sgorski.nethelt.webapi.notification.service.NotificationDeliveryService;
import pl.sgorski.nethelt.webapi.notification.service.NotificationPreferencesService;
import pl.sgorski.nethelt.webapi.notification.service.NotificationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountCreatedRequestListener {

  private final NotificationPreferencesService notificationPreferencesService;
  private final NotificationService notificationService;
  private final NotificationDeliveryService notificationDeliveryService;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(AccountCreatedEvent event) {
    log.debug("Handling AccountCreatedEvent for user with ID {}", event.userId());
    try {
      var preferences = notificationPreferencesService.getPreferences(event.userId());
      var notification = createWelcomeNotification(event.userId());
      notificationDeliveryService.send(notification, preferences.getEnabledChannels());
      log.debug(
          "Welcome notification sent for user with ID {} via channels: {}",
          event.userId(),
          preferences.getEnabledChannels());
    } catch (Exception e) {
      log.error(
          "Failed to send welcome notification for user with ID {}: {}",
          event.userId(),
          e.getMessage(),
          e);
    }
  }

  private Notification createWelcomeNotification(Long userId) {
    var command =
        new NotificationCommand(
            userId,
            "Welcome in Nethelt!",
            "Thank you for creating an account. You can log in now.");
    return notificationService.create(command);
  }
}
