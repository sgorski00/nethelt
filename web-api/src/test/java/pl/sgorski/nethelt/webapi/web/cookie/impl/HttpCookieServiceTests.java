package pl.sgorski.nethelt.webapi.web.cookie.impl;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.http.Cookie;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class HttpCookieServiceTests {

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private HttpCookieService httpCookieService;

  @BeforeEach
  void setUp() {
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    httpCookieService = new HttpCookieService();

    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));
  }

  @Test
  void save_shouldAddCookieToResponse() {
    httpCookieService.save("testCookie", "testValue", Duration.ofMinutes(5));

    var headers = response.getHeaders(HttpHeaders.SET_COOKIE);
    assertFalse(headers.isEmpty());
    assertTrue(headers.stream().anyMatch(header -> header.contains("testCookie")));
  }

  @Test
  void save_shouldAssignCorrectPropertiesToTheCookie() {
    httpCookieService.save("testCookie", "testValue", Duration.ofMinutes(5));

    response.getHeaders(HttpHeaders.SET_COOKIE).stream()
        .filter(header -> header.contains("testCookie"))
        .findFirst()
        .ifPresentOrElse(
            cookie -> {
              assertTrue(cookie.contains("Max-Age=300"));
              assertTrue(cookie.contains("HttpOnly"));
              assertTrue(cookie.contains("Secure"));
              assertTrue(cookie.contains("Path=/"));
              assertTrue(cookie.contains("SameSite=Lax"));
              assertTrue(cookie.contains("testValue"));
              assertTrue(cookie.contains("testCookie"));
            },
            () -> {
              throw new AssertionError("Cookie not found");
            });
  }

  @Test
  void find_shouldReturnCookieValue_whenCookieExists() {
    request.setCookies(
        new Cookie("testCookie", "testValue"), new Cookie("otherCookie", "otherValue"));

    var cookieValue = httpCookieService.find("testCookie");

    assertTrue(cookieValue.isPresent());
    assertEquals("testValue", cookieValue.get());
  }

  @Test
  void find_shouldReturnEmpty_whenCookieNotFound() {
    request.setCookies(new Cookie("wrongCookie", "testValue"));

    var cookieValue = httpCookieService.find("testCookie");

    assertFalse(cookieValue.isPresent());
  }

  @Test
  void find_shouldReturnEmpty_whenCookiesAreEmpty() {
    request.setCookies();

    var cookieValue = httpCookieService.find("testCookie");

    assertFalse(cookieValue.isPresent());
  }

  @Test
  void delete_shouldAddExpiredCookieToResponse() {
    httpCookieService.delete("testCookie");

    var headers = response.getHeaders(HttpHeaders.SET_COOKIE);
    assertFalse(headers.isEmpty());
    assertTrue(headers.stream().anyMatch(header -> header.contains("testCookie")));
    assertTrue(headers.stream().anyMatch(header -> header.contains("Max-Age=0")));
  }
}
