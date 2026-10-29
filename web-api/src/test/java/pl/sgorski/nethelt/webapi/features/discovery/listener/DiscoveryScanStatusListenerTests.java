package pl.sgorski.nethelt.webapi.features.discovery.listener;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.features.discovery.domain.DiscoveryScanStatus;
import pl.sgorski.nethelt.webapi.features.discovery.dto.event.DiscoveryScanStatusChangedEvent;
import pl.sgorski.nethelt.webapi.features.discovery.dto.response.DiscoveryScanStatusResponse;
import pl.sgorski.nethelt.webapi.web.websocket.UserMessageSender;

@ExtendWith(MockitoExtension.class)
public class DiscoveryScanStatusListenerTests {

  private final DiscoveryScanStatusChangedEvent event =
      new DiscoveryScanStatusChangedEvent(7L, 2L, "Home", DiscoveryScanStatus.RUNNING, 0, null);

  @Mock private UserMessageSender userMessageSender;
  @InjectMocks private DiscoveryScanStatusListener listener;

  @Test
  void handle_shouldPushStatusToNetworkOwner() {
    listener.handle(event);

    verify(userMessageSender)
        .send(
            7L,
            DiscoveryScanStatusListener.DESTINATION,
            new DiscoveryScanStatusResponse(2L, DiscoveryScanStatus.RUNNING));
  }

  @Test
  void handle_shouldNotThrow_whenSendingFails() {
    when(userMessageSender.send(anyLong(), anyString(), any()))
        .thenThrow(new RuntimeException("broker down"));

    assertDoesNotThrow(() -> listener.handle(event));
  }
}
