package pl.sgorski.nethelt.webapi.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.sgorski.nethelt.webapi.notification.dto.response.NotificationPreferencesResponse;
import pl.sgorski.nethelt.webapi.notification.mapper.NotificationMapper;
import pl.sgorski.nethelt.webapi.notification.service.NotificationPreferencesService;
import pl.sgorski.nethelt.webapi.security.authenticated.AuthenticatedUserResolver;

@RestController
@RequestMapping("/notifications/preferences")
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
}
