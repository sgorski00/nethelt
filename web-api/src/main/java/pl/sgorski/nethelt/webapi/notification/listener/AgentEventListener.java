package pl.sgorski.nethelt.webapi.notification.listener;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.sgorski.nethelt.webapi.features.agent.domain.AgentStatus;
import pl.sgorski.nethelt.webapi.features.agent.dto.event.AgentOfflineEvent;
import pl.sgorski.nethelt.webapi.features.agent.dto.event.AgentOnlineEvent;
import pl.sgorski.nethelt.webapi.features.agent.dto.event.AgentStatusChangedEvent;
import pl.sgorski.nethelt.webapi.features.agent.dto.event.AgentTokenRenewedEvent;
import pl.sgorski.nethelt.webapi.notification.dto.command.NotificationCommand;
import pl.sgorski.nethelt.webapi.notification.service.NotificationDeliveryService;
import pl.sgorski.nethelt.webapi.notification.service.NotificationPreferencesService;
import pl.sgorski.nethelt.webapi.notification.service.NotificationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgentEventListener {

  private final NotificationPreferencesService notificationPreferencesService;
  private final NotificationService notificationService;
  private final NotificationDeliveryService notificationDeliveryService;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(AgentStatusChangedEvent event) {
    var command =
        event.status() == AgentStatus.DISABLED
            ? new NotificationCommand(
                event.userId(),
                "[Nethelt] Agent disabled",
                String.format(
                    "Agent %s in network %s has been disabled. It cannot connect until you activate it again.",
                    event.agentName(), event.networkName()))
            : new NotificationCommand(
                event.userId(),
                "[Nethelt] Agent activated",
                String.format(
                    "Agent %s in network %s has been activated.",
                    event.agentName(), event.networkName()));
    send(command);
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(AgentTokenRenewedEvent event) {
    var command =
        new NotificationCommand(
            event.userId(),
            "[Nethelt] Agent token regenerated",
            String.format(
                "The token of agent %s in network %s has been regenerated. The previous"
                    + " token no longer works, register the new one on the agent machine.",
                event.agentName(), event.networkName()));
    send(command);
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(AgentOfflineEvent event) {
    var command =
        new NotificationCommand(
            event.userId(),
            "[Nethelt] Agent offline",
            String.format(
                "Agent %s in network %s has not sent a heartbeat for more than %s. Last heartbeat: %s.",
                event.agentName(),
                event.networkName(),
                format(event.maxUnavailabilityTime()),
                event.lastHeartbeatAt()));
    send(command);
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(AgentOnlineEvent event) {
    var command =
        new NotificationCommand(
            event.userId(),
            "[Nethelt] Agent back online",
            String.format(
                "Agent %s in network %s is back online.", event.agentName(), event.networkName()));
    send(command);
  }

  private void send(NotificationCommand command) {
    try {
      var preferences = notificationPreferencesService.getPreferences(command.userId());
      var notification = notificationService.create(command);
      notificationDeliveryService.send(notification, preferences.getEnabledChannels());
      log.debug(
          "Agent notification {} sent for user with ID {} via channels: {}",
          command.title(),
          command.userId(),
          preferences.getEnabledChannels());
    } catch (Exception e) {
      log.error(
          "Failed to send agent notification {} for user with ID {}: {}",
          command.title(),
          command.userId(),
          e.getMessage(),
          e);
    }
  }

  private String format(Duration duration) {
    var seconds = duration.toSeconds();
    return seconds % 60 == 0 ? (seconds / 60) + " min" : seconds + " s";
  }
}
