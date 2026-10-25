package pl.sgorski.nethelt.webapi.features.agent.listener;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.features.agent.domain.AgentConnectionState;
import pl.sgorski.nethelt.webapi.features.agent.dto.event.AgentConnectionStateChangedEvent;
import pl.sgorski.nethelt.webapi.features.agent.dto.response.AgentConnectionStateResponse;
import pl.sgorski.nethelt.webapi.web.websocket.UserMessageSender;

@ExtendWith(MockitoExtension.class)
public class AgentConnectionStateListenerTests {

  private final AgentConnectionStateChangedEvent event =
      new AgentConnectionStateChangedEvent(7L, 2L, 1L, AgentConnectionState.OFFLINE);

  @Mock private UserMessageSender userMessageSender;
  @InjectMocks private AgentConnectionStateListener listener;

  @Test
  void handle_shouldPushConnectionStateToNetworkOwner() {
    listener.handle(event);

    verify(userMessageSender)
        .send(
            7L,
            AgentConnectionStateListener.DESTINATION,
            new AgentConnectionStateResponse(2L, 1L, AgentConnectionState.OFFLINE));
  }

  @Test
  void handle_shouldNotThrow_whenSendingFails() {
    when(userMessageSender.send(anyLong(), anyString(), any()))
        .thenThrow(new RuntimeException("broker down"));

    assertDoesNotThrow(() -> listener.handle(event));
  }
}
