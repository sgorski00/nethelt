package pl.sgorski.nethelt.webapi.notification.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.sgorski.nethelt.webapi.features.discovery.dto.event.DiscoveryScanStatusChangedEvent;
import pl.sgorski.nethelt.webapi.notification.dto.command.NotificationCommand;
import pl.sgorski.nethelt.webapi.notification.service.NotificationDeliveryService;
import pl.sgorski.nethelt.webapi.notification.service.NotificationPreferencesService;
import pl.sgorski.nethelt.webapi.notification.service.NotificationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscoveryScanEventListener {

  private final NotificationPreferencesService notificationPreferencesService;
  private final NotificationService notificationService;
  private final NotificationDeliveryService notificationDeliveryService;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(DiscoveryScanStatusChangedEvent event) {
    switch (event.status()) {
      case COMPLETED ->
          send(
              new NotificationCommand(
                  event.userId(),
                  "[Nethelt] Network scan finished",
                  String.format(
                      "Scan of network %s has finished. Found devices: %d.",
                      event.networkName(), event.resultCount())));
      case FAILED ->
          send(
              new NotificationCommand(
                  event.userId(),
                  "[Nethelt] Network scan failed",
                  String.format(
                      "Scan of network %s has failed: %s",
                      event.networkName(), event.failureReason())));
      case TIMED_OUT ->
          send(
              new NotificationCommand(
                  event.userId(),
                  "[Nethelt] Network scan timed out",
                  String.format(
                      "Scan of network %s did not finish in time. You can start a new scan.",
                      event.networkName())));
      case PENDING, RUNNING -> {}
    }
  }

  private void send(NotificationCommand command) {
    try {
      var preferences = notificationPreferencesService.getPreferences(command.userId());
      var notification = notificationService.create(command);
      notificationDeliveryService.send(notification, preferences.getEnabledChannels());
      log.debug(
          "Discovery scan notification {} sent for user with ID {} via channels: {}",
          command.title(),
          command.userId(),
          preferences.getEnabledChannels());
    } catch (Exception e) {
      log.error(
          "Failed to send discovery scan notification {} for user with ID {}: {}",
          command.title(),
          command.userId(),
          e.getMessage(),
          e);
    }
  }
}
