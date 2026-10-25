package pl.sgorski.nethelt.webapi.security.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.webapi.features.auth.service.AccessTokenService;

@Component
@RequiredArgsConstructor
public final class StompAuthenticationInterceptor implements ChannelInterceptor {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";
  private static final String USER_DESTINATION_PREFIX = "/user/";

  private final AccessTokenService accessTokenService;
  private final UserDetailsService userDetailsService;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    var accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
    if (accessor == null || accessor.getCommand() == null) {
      return message;
    }

    switch (accessor.getCommand()) {
      case CONNECT -> accessor.setUser(authenticate(accessor));
      case SUBSCRIBE -> requireUserDestination(accessor);
      case SEND -> throw new AccessDeniedException("Sending messages is not allowed");
      default -> {}
    }
    return message;
  }

  private UsernamePasswordAuthenticationToken authenticate(StompHeaderAccessor accessor) {
    var header = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);
    if (header == null || !header.startsWith(BEARER_PREFIX)) {
      throw new AuthenticationCredentialsNotFoundException("Missing access token");
    }

    var token = header.substring(BEARER_PREFIX.length());
    if (!accessTokenService.isValid(token)) {
      throw new BadCredentialsException("Invalid access token");
    }

    var email = accessTokenService.getEmailFromToken(token);
    var userDetails = userDetailsService.loadUserByUsername(email);
    var userId = accessTokenService.getUserIdFromToken(token).toString();
    return new UsernamePasswordAuthenticationToken(userId, null, userDetails.getAuthorities());
  }

  private void requireUserDestination(StompHeaderAccessor accessor) {
    if (accessor.getUser() == null) {
      throw new AuthenticationCredentialsNotFoundException("Not authenticated");
    }
    var destination = accessor.getDestination();
    if (destination == null || !destination.startsWith(USER_DESTINATION_PREFIX)) {
      throw new AccessDeniedException("Subscribing to " + destination + " is not allowed");
    }
  }
}
