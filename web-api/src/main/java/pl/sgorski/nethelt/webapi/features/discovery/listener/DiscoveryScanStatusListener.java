package pl.sgorski.nethelt.webapi.features.discovery.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.sgorski.nethelt.webapi.features.discovery.dto.event.DiscoveryScanStatusChangedEvent;
import pl.sgorski.nethelt.webapi.features.discovery.dto.response.DiscoveryScanStatusResponse;
import pl.sgorski.nethelt.webapi.web.websocket.UserMessageSender;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscoveryScanStatusListener {

  public static final String DESTINATION = "/queue/discovery-scans";

  private final UserMessageSender userMessageSender;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(DiscoveryScanStatusChangedEvent event) {
    try {
      var payload = new DiscoveryScanStatusResponse(event.networkId(), event.status());
      if (!userMessageSender.send(event.userId(), DESTINATION, payload)) {
        log.debug(
            "User with ID {} is not connected, skipping discovery scan status push",
            event.userId());
      }
    } catch (Exception e) {
      log.warn(
          "Failed to push discovery scan status of network with ID {}: {}",
          event.networkId(),
          e.getMessage(),
          e);
    }
  }
}
