package pl.sgorski.nethelt.webapi.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationChannel;
import pl.sgorski.nethelt.webapi.notification.dto.response.NotificationPreferencesResponse;
import pl.sgorski.nethelt.webapi.notification.mapper.NotificationMapper;
import pl.sgorski.nethelt.webapi.notification.service.NotificationPreferencesService;
import pl.sgorski.nethelt.webapi.security.authenticated.AuthenticatedUserResolver;

@SuppressWarnings("SpringMvcPathVariableDeclarationInspection")
@RestController
@RequestMapping(value = "/notifications/preferences", version = "1")
@RequiredArgsConstructor
public class NotificationPreferencesController {

  private final AuthenticatedUserResolver authenticatedUserResolver;
  private final NotificationPreferencesService notificationPreferencesService;
  private final NotificationMapper notificationMapper;

  @GetMapping
  public ResponseEntity<NotificationPreferencesResponse> getNotificationPreferences(
      Authentication authentication) {
    var userId = authenticatedUserResolver.requireUserId(authentication);
    var preferences = notificationPreferencesService.getPreferences(userId);
    return ResponseEntity.ok(notificationMapper.toResponse(preferences));
  }

  @PutMapping("/{channel}")
  public ResponseEntity<NotificationPreferencesResponse> enable(
      Authentication authentication, @PathVariable("channel") NotificationChannel channel) {
    var userId = authenticatedUserResolver.requireUserId(authentication);
    var preferences = notificationPreferencesService.enableChannel(userId, channel);
    return ResponseEntity.ok(notificationMapper.toResponse(preferences));
  }

  @DeleteMapping("/{channel}")
  public ResponseEntity<NotificationPreferencesResponse> disable(
      Authentication authentication, @PathVariable("channel") NotificationChannel channel) {
    var userId = authenticatedUserResolver.requireUserId(authentication);
    var preferences = notificationPreferencesService.disableChannel(userId, channel);
    return ResponseEntity.ok(notificationMapper.toResponse(preferences));
  }
}
