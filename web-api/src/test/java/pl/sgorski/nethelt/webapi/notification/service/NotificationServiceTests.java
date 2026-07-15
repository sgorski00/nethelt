package pl.sgorski.nethelt.webapi.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.exception.domain.UserNotFoundException;
import pl.sgorski.nethelt.webapi.exception.notification.NotificationNotFoundException;
import pl.sgorski.nethelt.webapi.features.user.service.UserService;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationChannel;
import pl.sgorski.nethelt.webapi.notification.dto.command.NotificationCommand;
import pl.sgorski.nethelt.webapi.notification.repository.NotificationRepository;
import pl.sgorski.nethelt.webapi.utils.TestNotificationFactory;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTests {

  @Mock private NotificationSender notificationSender;
  @Mock private NotificationRepository notificationRepository;
  @Mock private UserService userService;
  private NotificationService notificationService;

  @BeforeEach
  void setUp() {
    this.notificationService =
        new NotificationService(List.of(notificationSender), notificationRepository, userService);
  }

  @Test
  void send_shouldCallSendersForSupportedChannels_whenChannelIsSupported() {
    var notification = TestNotificationFactory.createNotification();
    when(notificationSender.supports(NotificationChannel.EMAIL)).thenReturn(true);

    notificationService.send(notification, Set.of(NotificationChannel.EMAIL));

    verify(notificationSender).send(notification);
  }

  @Test
  void send_shouldNotCallSenders_whenChannelIsNotSupported() {
    var notification = TestNotificationFactory.createNotification();
    when(notificationSender.supports(NotificationChannel.EMAIL)).thenReturn(false);

    notificationService.send(notification, Set.of(NotificationChannel.EMAIL));

    verify(notificationSender, never()).send(notification);
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
}
