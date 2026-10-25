package pl.sgorski.nethelt.webapi.web.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMessageSender {

  private final SimpMessagingTemplate messagingTemplate;
  private final SimpUserRegistry userRegistry;

  /**
   * @return {@code false} when the user has no open session, so nothing was sent
   */
  public boolean send(Long userId, String destination, Object payload) {
    var user = userId.toString();
    if (userRegistry.getUser(user) == null) {
      return false;
    }
    messagingTemplate.convertAndSendToUser(user, destination, payload);
    return true;
  }
}
