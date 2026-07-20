package pl.sgorski.nethelt.webapi.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import pl.sgorski.nethelt.webapi.exception.domain.user.UserNotFoundException;
import pl.sgorski.nethelt.webapi.exception.notification.NotificationNotFoundException;
import pl.sgorski.nethelt.webapi.features.user.service.UserService;
import pl.sgorski.nethelt.webapi.notification.dto.command.NotificationCommand;
import pl.sgorski.nethelt.webapi.notification.repository.NotificationRepository;
import pl.sgorski.nethelt.webapi.utils.TestNotificationFactory;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTests {

  @Mock private NotificationRepository notificationRepository;
  @Mock private UserService userService;
  @InjectMocks private NotificationService notificationService;

  @Test
  void getNotifications_shouldReturnNotificationsForUser_whenShowReadIsTrue() {
    var user = TestUserFactory.createLocalUser();
    var notification1 = TestNotificationFactory.createNotification(user);
    var notification2 = TestNotificationFactory.createNotification(user);
    var pageable = Pageable.ofSize(10);
    when(notificationRepository.findByUserId(user.getId(), true, pageable))
        .thenReturn(new PageImpl<>(List.of(notification1, notification2)));

    var result = notificationService.getNotifications(user.getId(), true, pageable);

    assertEquals(2, result.getTotalElements());
    assertTrue(result.getContent().contains(notification1));
    assertTrue(result.getContent().contains(notification2));
  }

  @Test
  void getNotifications_shouldReturnNotificationsForUser_whenShowReadIsFalse() {
    var user = TestUserFactory.createLocalUser();
    var notification1 = TestNotificationFactory.createNotification(user);
    var notification2 = TestNotificationFactory.createNotification(user);
    var pageable = Pageable.ofSize(10);
    when(notificationRepository.findByUserId(user.getId(), false, pageable))
        .thenReturn(new PageImpl<>(List.of(notification1, notification2)));

    var result = notificationService.getNotifications(user.getId(), false, pageable);

    assertEquals(2, result.getTotalElements());
    assertTrue(result.getContent().contains(notification1));
    assertTrue(result.getContent().contains(notification2));
  }

  @Test
  void getNotifications_shouldReturnEmptyList_whenNotFound() {
    var pageable = Pageable.ofSize(10);
    when(notificationRepository.findByUserId(1L, true, pageable)).thenReturn(Page.empty());

    var result = notificationService.getNotifications(1L, true, pageable);

    assertTrue(result.isEmpty());
  }

  @Test
  void create_shouldCreateAndSaveNotificationWithCorrectValues_whenUserPresent() {
    var user = TestUserFactory.createLocalUser();
    when(userService.getUser(1L)).thenReturn(user);
    var command = new NotificationCommand(1L, "Test Title", "Test Content");
    when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var result = notificationService.create(command);

    assertEquals("Test Title", result.getTitle());
    assertEquals("Test Content", result.getContent());
    assertSame(user, result.getUser());
    verify(notificationRepository).save(any());
  }

  @Test
  void create_shouldThrow_whenUserNotFound() {
    when(userService.getUser(1L)).thenThrow(UserNotFoundException.class);
    var command = new NotificationCommand(1L, "Test Title", "Test Content");

    assertThrows(UserNotFoundException.class, () -> notificationService.create(command));
  }

  @Test
  void read_shouldMarkNotificationAsRead_whenNotificationExists() {
    var notification = TestNotificationFactory.createNotification();
    when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

    assertFalse(notification.isRead());

    notificationService.read(1L);

    assertTrue(notification.isRead());
  }

  @Test
  void read_shouldThrow_whenNotificationNotFound() {
    when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(NotificationNotFoundException.class, () -> notificationService.read(1L));
  }

  @Test
  void getUnreadNotificationsCount_shouldReturnCount_whenUserExists() {
    var userId = 1L;
    when(notificationRepository.countByUserIdAndReadAtIsNull(userId)).thenReturn(5L);

    var result = notificationService.getUnreadNotificationsCount(userId);

    assertEquals(5L, result);
  }
}
