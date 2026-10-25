package pl.sgorski.nethelt.webapi.notification.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import pl.sgorski.nethelt.webapi.config.PostgresIntegrationTest;
import pl.sgorski.nethelt.webapi.exception.notification.NotificationDeliverySkippedException;
import pl.sgorski.nethelt.webapi.features.auth.service.AccessTokenService;
import pl.sgorski.nethelt.webapi.features.user.domain.User;
import pl.sgorski.nethelt.webapi.features.user.repository.UserRepository;
import pl.sgorski.nethelt.webapi.notification.domain.Notification;
import pl.sgorski.nethelt.webapi.notification.repository.NotificationRepository;
import pl.sgorski.nethelt.webapi.utils.TestNotificationFactory;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketNotificationSenderIntegrationTests extends PostgresIntegrationTest {

  @LocalServerPort private int port;
  @Autowired private UserRepository userRepository;
  @Autowired private NotificationRepository notificationRepository;
  @Autowired private AccessTokenService accessTokenService;
  @Autowired private WebSocketNotificationSender webSocketNotificationSender;
  @Autowired private SimpUserRegistry userRegistry;

  private WebSocketStompClient stompClient;
  private User user;

  @BeforeEach
  void setUp() {
    stompClient = new WebSocketStompClient(new StandardWebSocketClient());
    stompClient.setMessageConverter(new JacksonJsonMessageConverter());
    user =
        userRepository.save(TestUserFactory.createLocalUser("ws-" + System.nanoTime() + "@x.pl"));
  }

  @AfterEach
  void tearDown() {
    stompClient.stop();
  }

  @Test
  void send_shouldPushNotificationToConnectedUser() throws Exception {
    var received = new LinkedBlockingQueue<Map<?, ?>>();
    var session = connect(bearer(accessTokenService.generateAccessToken(user)));
    subscribe(session, user, received);
    var notification = saveNotification(user);

    webSocketNotificationSender.send(notification);

    var payload = received.poll(5, TimeUnit.SECONDS);
    assertNotNull(payload);
    assertEquals(notification.getId().intValue(), payload.get("id"));
    assertEquals(notification.getTitle(), payload.get("title"));
    assertInstanceOf(String.class, payload.get("createdAt"));
  }

  @Test
  void send_shouldNotPushToOtherUsers() throws Exception {
    var received = new LinkedBlockingQueue<Map<?, ?>>();
    var other =
        userRepository.save(
            TestUserFactory.createLocalUser("ws-other-" + System.nanoTime() + "@x.pl"));
    var session = connect(bearer(accessTokenService.generateAccessToken(other)));
    subscribe(session, other, received);
    connect(bearer(accessTokenService.generateAccessToken(user)));

    webSocketNotificationSender.send(saveNotification(user));

    assertNull(received.poll(1, TimeUnit.SECONDS));
  }

  @Test
  void send_shouldSkip_whenUserIsNotConnected() {
    var notification = saveNotification(user);

    assertThrows(
        NotificationDeliverySkippedException.class,
        () -> webSocketNotificationSender.send(notification));
  }

  @Test
  void connect_shouldFail_whenAccessTokenIsInvalid() {
    var result =
        stompClient.connectAsync(
            url(),
            new WebSocketHttpHeaders(),
            bearer("invalid"),
            new StompSessionHandlerAdapter() {});

    assertThrows(ExecutionException.class, () -> result.get(5, TimeUnit.SECONDS));
  }

  private StompSession connect(StompHeaders headers) throws Exception {
    return stompClient
        .connectAsync(
            url(), new WebSocketHttpHeaders(), headers, new StompSessionHandlerAdapter() {})
        .get(5, TimeUnit.SECONDS);
  }

  private void subscribe(StompSession session, User subscriber, BlockingQueue<Map<?, ?>> received)
      throws InterruptedException {
    session.subscribe(
        "/user" + WebSocketNotificationSender.DESTINATION,
        new StompFrameHandler() {
          @Override
          public Type getPayloadType(StompHeaders headers) {
            return Map.class;
          }

          @Override
          public void handleFrame(StompHeaders headers, Object payload) {
            received.add((Map<?, ?>) payload);
          }
        });
    // the simple broker does not send receipts, so wait until the subscription is registered
    for (var i = 0; i < 50 && !isSubscribed(subscriber); i++) {
      Thread.sleep(100);
    }
    assertTrue(isSubscribed(subscriber));
  }

  private boolean isSubscribed(User subscriber) {
    var simpUser = userRegistry.getUser(subscriber.getId().toString());
    return simpUser != null
        && simpUser.getSessions().stream().anyMatch(s -> !s.getSubscriptions().isEmpty());
  }

  private Notification saveNotification(User recipient) {
    return notificationRepository.save(TestNotificationFactory.createNotification(recipient));
  }

  private StompHeaders bearer(String token) {
    var headers = new StompHeaders();
    headers.add("Authorization", "Bearer " + token);
    return headers;
  }

  private String url() {
    return "ws://localhost:" + port + "/api/ws";
  }
}
