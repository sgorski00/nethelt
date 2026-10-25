package pl.sgorski.nethelt.webapi.security.websocket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.security.Principal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import pl.sgorski.nethelt.webapi.features.auth.service.AccessTokenService;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;

@ExtendWith(MockitoExtension.class)
public class StompAuthenticationInterceptorTests {

  @Mock private AccessTokenService accessTokenService;
  @Mock private UserDetailsService userDetailsService;
  @Mock private MessageChannel channel;
  @InjectMocks private StompAuthenticationInterceptor interceptor;

  @Test
  void preSend_shouldAuthenticateConnect_whenAccessTokenIsValid() {
    var user = TestUserFactory.createLocalUser();
    when(accessTokenService.isValid("token")).thenReturn(true);
    when(accessTokenService.getEmailFromToken("token")).thenReturn(user.getEmail());
    when(userDetailsService.loadUserByUsername(user.getEmail())).thenReturn(user);
    var accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
    accessor.addNativeHeader("Authorization", "Bearer token");

    interceptor.preSend(message(accessor), channel);

    var auth = assertInstanceOf(UsernamePasswordAuthenticationToken.class, accessor.getUser());
    assertSame(user, auth.getPrincipal());
    assertEquals(user.getEmail(), auth.getName());
  }

  @Test
  void preSend_shouldRejectConnect_whenAuthorizationHeaderNotFound() {
    var accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
    var message = message(accessor);

    assertThrows(
        AuthenticationCredentialsNotFoundException.class,
        () -> interceptor.preSend(message, channel));
    verify(accessTokenService, never()).isValid(anyString());
  }

  @Test
  void preSend_shouldRejectConnect_whenAuthorizationIsNotBearer() {
    var accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
    accessor.addNativeHeader("Authorization", "Agent token");
    var message = message(accessor);

    assertThrows(
        AuthenticationCredentialsNotFoundException.class,
        () -> interceptor.preSend(message, channel));
  }

  @Test
  void preSend_shouldRejectConnect_whenAccessTokenIsInvalid() {
    when(accessTokenService.isValid("token")).thenReturn(false);
    var accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
    accessor.addNativeHeader("Authorization", "Bearer token");
    var message = message(accessor);

    assertThrows(BadCredentialsException.class, () -> interceptor.preSend(message, channel));
    verify(userDetailsService, never()).loadUserByUsername(anyString());
  }

  @Test
  void preSend_shouldAllowSubscribe_whenDestinationIsUserDestination() {
    var accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
    accessor.setUser(mock(Principal.class));
    accessor.setDestination("/user/queue/notifications");
    var message = message(accessor);

    var result = interceptor.preSend(message, channel);

    assertSame(message, result);
  }

  @Test
  void preSend_shouldRejectSubscribe_whenDestinationIsNotUserDestination() {
    var accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
    accessor.setUser(mock(Principal.class));
    accessor.setDestination("/queue/notifications-user123");
    var message = message(accessor);

    assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, channel));
  }

  @Test
  void preSend_shouldRejectSubscribe_whenNotAuthenticated() {
    var accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
    accessor.setDestination("/user/queue/notifications");
    var message = message(accessor);

    assertThrows(
        AuthenticationCredentialsNotFoundException.class,
        () -> interceptor.preSend(message, channel));
  }

  @Test
  void preSend_shouldRejectSend() {
    var accessor = StompHeaderAccessor.create(StompCommand.SEND);
    accessor.setUser(mock(Principal.class));
    accessor.setDestination("/queue/notifications");
    var message = message(accessor);

    assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, channel));
  }

  private Message<byte[]> message(StompHeaderAccessor accessor) {
    accessor.setLeaveMutable(true);
    return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
  }
}
