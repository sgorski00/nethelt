package pl.sgorski.nethelt.webapi.features.agent.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.sgorski.nethelt.webapi.features.agent.dto.event.AgentConnectionStateChangedEvent;
import pl.sgorski.nethelt.webapi.features.agent.dto.response.AgentConnectionStateResponse;
import pl.sgorski.nethelt.webapi.web.websocket.UserMessageSender;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgentConnectionStateListener {

  public static final String DESTINATION = "/queue/agents";

  private final UserMessageSender userMessageSender;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(AgentConnectionStateChangedEvent event) {
    try {
      var payload =
          new AgentConnectionStateResponse(
              event.networkId(), event.agentId(), event.connectionState());
      if (!userMessageSender.send(event.userId(), DESTINATION, payload)) {
        log.debug(
            "User with ID {} is not connected, skipping agent connection state push",
            event.userId());
      }
    } catch (Exception e) {
      log.warn(
          "Failed to push connection state of agent with ID {}: {}",
          event.agentId(),
          e.getMessage(),
          e);
    }
  }
}
