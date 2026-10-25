package pl.sgorski.nethelt.webapi.web.websocket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;

@ExtendWith(MockitoExtension.class)
public class UserMessageSenderTests {

  @Mock private SimpMessagingTemplate messagingTemplate;
  @Mock private SimpUserRegistry userRegistry;
  @InjectMocks private UserMessageSender userMessageSender;

  @Test
  void send_shouldSendToUserDestination_whenUserIsConnected() {
    when(userRegistry.getUser("7")).thenReturn(mock(SimpUser.class));

    var result = userMessageSender.send(7L, "/queue/test", "payload");

    assertTrue(result);
    verify(messagingTemplate).convertAndSendToUser("7", "/queue/test", "payload");
  }

  @Test
  void send_shouldNotSend_whenUserIsNotConnected() {
    when(userRegistry.getUser("7")).thenReturn(null);

    var result = userMessageSender.send(7L, "/queue/test", "payload");

    assertFalse(result);
    verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
  }
}
