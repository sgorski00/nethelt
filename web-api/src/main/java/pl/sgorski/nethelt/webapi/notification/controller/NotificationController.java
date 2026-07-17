package pl.sgorski.nethelt.webapi.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import pl.sgorski.nethelt.webapi.notification.dto.response.NotificationResponse;
import pl.sgorski.nethelt.webapi.notification.mapper.NotificationMapper;
import pl.sgorski.nethelt.webapi.notification.service.NotificationService;
import pl.sgorski.nethelt.webapi.security.authenticated.AuthenticatedUserResolver;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final AuthenticatedUserResolver authenticatedUserResolver;
  private final NotificationService notificationService;
  private final NotificationMapper notificationMapper;

  @GetMapping
  public ResponseEntity<Page<NotificationResponse>> getNotifications(
      Authentication authentication,
      Pageable pageable,
      @RequestParam(name = "showRead", defaultValue = "true", required = false) Boolean showRead) {
    var userId = authenticatedUserResolver.requireUserId(authentication);
    var notifications =
        notificationService
            .getNotifications(userId, showRead, pageable)
            .map(notificationMapper::toResponse);
    return ResponseEntity.ok(notifications);
  }

  @GetMapping("/unread-count")
  public ResponseEntity<Long> getUnreadNotificationsCount(Authentication authentication) {
    var userId = authenticatedUserResolver.requireUserId(authentication);
    var unreadCount = notificationService.getUnreadNotificationsCount(userId);
    return ResponseEntity.ok(unreadCount);
  }

  @PutMapping("/{notificationId}")
  @PreAuthorize("@notificationAuthorization.isOwner(authentication, #notificationId)")
  public ResponseEntity<Void> markNotificationAsRead(
      @P("notificationId") @PathVariable(name = "notificationId") Long notificationId) {
    notificationService.read(notificationId);
    return ResponseEntity.noContent().build();
  }
}
