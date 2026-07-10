package pl.sgorski.nethelt.webapi.security.oauth2;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

public class HttpCookieOAuth2AuthorizationRequestRepositoryTests {

  private HttpServletRequest request;
  private HttpServletResponse response;
  private HttpCookieOAuth2AuthorizationRequestRepository repository;

  @BeforeEach
  void setUp() {
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    repository = new HttpCookieOAuth2AuthorizationRequestRepository();
  }

  @Test
  void saveAuthorizationRequest_shouldAddClearCookie_whenAuthorizationRequestIsNull() {
    repository.saveAuthorizationRequest(null, request, response);

    verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), contains("Max-Age=0"));
  }

  @Test
  void saveAuthorizationRequest_shouldSaveAuthorizationRequest_whenAuthorizationRequestIsNotNull() {
    var auth = createTestAuthorizationRequest();

    var captor = ArgumentCaptor.forClass(String.class);

    repository.saveAuthorizationRequest(auth, request, response);

    verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), captor.capture());

    var cookieHeader = captor.getValue();

    assertTrue(cookieHeader.contains("HttpOnly"));
    assertTrue(cookieHeader.contains("Secure"));
    assertTrue(cookieHeader.contains("SameSite=Lax"));
    assertTrue(cookieHeader.contains("Path=/"));
  }

  @Test
  void loadAuthorizationRequest_shouldReturnNull_whenCookiesAreNull() {
    when(request.getCookies()).thenReturn(null);

    var result = repository.loadAuthorizationRequest(request);

    assertNull(result);
  }

  @Test
  void
      loadAuthorizationRequest_shouldReturnNull_whenOAuth2AuthorizationRequestCookieDoesNotExist() {
    var cookie = new Cookie("some_other_cookie", "value");
    var cookies = new Cookie[] {cookie};
    when(request.getCookies()).thenReturn(cookies);

    var result = repository.loadAuthorizationRequest(request);

    assertNull(result);
  }

  @Test
  void loadAuthorizationRequest_shouldReturnOAuth2AuthorizationRequest_whenCookieExists() {
    var auth = createTestAuthorizationRequest();
    var cookieValue = saveCookieAndGetCookieValue(auth);
    var cookie = createOAuth2RequestCookie(cookieValue);
    var cookies = new Cookie[] {cookie};
    when(request.getCookies()).thenReturn(cookies);

    var result = repository.loadAuthorizationRequest(request);

    assertEquals(auth, result);
  }

  @Test
  void loadAuthorizationRequest_shouldThrowIllegalStateException_whenCookieContainsInvalidData() {
    var cookie = createOAuth2RequestCookie("invalid-data");
    var cookies = new Cookie[] {cookie};
    when(request.getCookies()).thenReturn(cookies);

    assertThrows(IllegalStateException.class, () -> repository.loadAuthorizationRequest(request));
  }

  @Test
  void
      removeAuthorizationRequest_shouldReturnNullAndClearCookie_whenAuthorizationRequestDoesNotExist() {
    when(request.getCookies()).thenReturn(null);

    var result = repository.removeAuthorizationRequest(request, response);

    assertNull(result);
    verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), contains("Max-Age=0"));
  }

  @Test
  void
      removeAuthorizationRequest_shouldReturnAuthorizationRequestAndClearCookie_whenAuthorizationRequestExists() {
    var auth = createTestAuthorizationRequest();
    var cookieValue = saveCookieAndGetCookieValue(auth);
    var cookie = createOAuth2RequestCookie(cookieValue);
    var cookies = new Cookie[] {cookie};
    when(request.getCookies()).thenReturn(cookies);

    var result = repository.removeAuthorizationRequest(request, response);

    assertEquals(auth, result);
    verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), contains("Max-Age=0"));
  }

  private String saveCookieAndGetCookieValue(OAuth2AuthorizationRequest auth) {
    var serializedDataCaptor = ArgumentCaptor.forClass(String.class);
    repository.saveAuthorizationRequest(auth, request, response);

    verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), serializedDataCaptor.capture());
    return serializedDataCaptor.getValue().split("oauth2_auth_request=")[1].split(";")[0];
  }

  private Cookie createOAuth2RequestCookie(String value) {
    return new Cookie("oauth2_auth_request", value);
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
