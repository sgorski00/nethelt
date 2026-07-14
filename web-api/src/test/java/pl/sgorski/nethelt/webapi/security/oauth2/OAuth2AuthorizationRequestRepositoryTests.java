package pl.sgorski.nethelt.webapi.security.oauth2;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.config.OAuth2Properties;
import pl.sgorski.nethelt.webapi.web.cookie.CookieService;

@ExtendWith(MockitoExtension.class)
public class OAuth2AuthorizationRequestRepositoryTests {

  public static final String OAUTH2_AUTH_REQUEST_KEY = "oauth2_auth_request";
  @Mock private CookieService cookieService;
  @Mock private OAuth2Properties oAuth2Properties;
  @InjectMocks private OAuth2AuthorizationRequestRepository repository;

  @Test
  void saveAuthorizationRequest_shouldClearCookie_whenAuthorizationRequestIsNull() {
    repository.saveAuthorizationRequest(null, null, null);

    verify(cookieService).delete(OAUTH2_AUTH_REQUEST_KEY);
  }

  @Test
  void
      saveAuthorizationRequest_shouldSaveSerializedAuthorizationRequest_whenAuthorizationRequestIsNotNull() {
    when(oAuth2Properties.authorizationRequestExpiration()).thenReturn(Duration.ofMinutes(5));
    var auth = createTestAuthorizationRequest();

    repository.saveAuthorizationRequest(auth, null, null);

    verify(cookieService).save(eq(OAUTH2_AUTH_REQUEST_KEY), anyString(), any());
  }

  @Test
  void loadAuthorizationRequest_shouldReturnNull_whenCookieDoesNotExist() {
    when(cookieService.find(OAUTH2_AUTH_REQUEST_KEY)).thenReturn(Optional.empty());

    var result = repository.loadAuthorizationRequest(null);

    assertNull(result);
  }

  @Test
  void loadAuthorizationRequest_shouldReturnAuthorizationRequest_whenCookieExists() {
    when(oAuth2Properties.authorizationRequestExpiration()).thenReturn(Duration.ofMinutes(5));
    var auth = createTestAuthorizationRequest();
    var serializedValue = captureSerializedValue(auth);

    when(cookieService.find(OAUTH2_AUTH_REQUEST_KEY)).thenReturn(Optional.of(serializedValue));

    var result = repository.loadAuthorizationRequest(null);

    assertEquals(auth, result);
  }

  @Test
  void loadAuthorizationRequest_shouldThrowIllegalStateException_whenCookieContainsInvalidData() {
    when(cookieService.find(OAUTH2_AUTH_REQUEST_KEY)).thenReturn(Optional.of("invalid-data"));

    assertThrows(IllegalStateException.class, () -> repository.loadAuthorizationRequest(null));
  }

  @Test
  void
      removeAuthorizationRequest_shouldReturnNullAndClearCookie_whenAuthorizationRequestDoesNotExist() {
    when(cookieService.find(OAUTH2_AUTH_REQUEST_KEY)).thenReturn(Optional.empty());

    var result = repository.removeAuthorizationRequest(null, null);

    assertNull(result);
    verify(cookieService).delete(OAUTH2_AUTH_REQUEST_KEY);
  }

  @Test
  void
      removeAuthorizationRequest_shouldReturnAuthorizationRequestAndClearCookie_whenAuthorizationRequestExists() {
    when(oAuth2Properties.authorizationRequestExpiration()).thenReturn(Duration.ofMinutes(5));
    var auth = createTestAuthorizationRequest();
    var serializedValue = captureSerializedValue(auth);

    when(cookieService.find(OAUTH2_AUTH_REQUEST_KEY)).thenReturn(Optional.of(serializedValue));

    var result = repository.removeAuthorizationRequest(null, null);

    assertEquals(auth, result);
    verify(cookieService).delete(OAUTH2_AUTH_REQUEST_KEY);
  }

  private String captureSerializedValue(OAuth2AuthorizationRequest auth) {
    var captor = ArgumentCaptor.forClass(String.class);

    repository.saveAuthorizationRequest(auth, null, null);

    verify(cookieService).save(eq(OAUTH2_AUTH_REQUEST_KEY), captor.capture(), any());

    return captor.getValue();
  }

  private OAuth2AuthorizationRequest createTestAuthorizationRequest() {
    return OAuth2AuthorizationRequest.authorizationCode()
        .authorizationUri("https://example.com/oauth2/authorize")
        .clientId("test-client-id")
        .redirectUri("https://example.com/oauth2/callback")
        .scopes(Set.of("email"))
        .state("test-state")
        .build();
  }
}
