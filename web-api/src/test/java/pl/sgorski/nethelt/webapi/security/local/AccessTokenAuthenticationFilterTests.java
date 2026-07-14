package pl.sgorski.nethelt.webapi.security.local;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import pl.sgorski.nethelt.webapi.features.auth.service.AccessTokenService;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;

@ExtendWith(MockitoExtension.class)
public class AccessTokenAuthenticationFilterTests {

  private static final String AUTHORIZATION_HEADER = "Authorization";

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  @Mock private FilterChain filterChain;
  @Mock private AccessTokenService accessTokenService;
  @Mock private UserDetailsService userDetailsService;
  @InjectMocks private AccessTokenAuthenticationFilter filter;

  @BeforeEach
  void setUp() {
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
  }

  @Test
  void doFilter_shouldFilterWithoutAuthentication_whenAuthorizationHeaderNotFound()
      throws ServletException, IOException {
    request.removeHeader(AUTHORIZATION_HEADER);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(accessTokenService, never()).isValid(anyString());
    verify(userDetailsService, never()).loadUserByUsername(anyString());
  }

  @Test
  void doFilter_shouldFilterWithoutAuthentication_whenAuthorizationIsNotBearer()
      throws ServletException, IOException {
    request.addHeader(AUTHORIZATION_HEADER, "not-a-bearer token");

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(accessTokenService, never()).isValid(anyString());
    verify(userDetailsService, never()).loadUserByUsername(anyString());
  }

  @Test
  void doFilter_shouldFilterWithoutAuthentication_whenTokenIsNotValid()
      throws ServletException, IOException {
    request.addHeader(AUTHORIZATION_HEADER, "Bearer token");
    when(accessTokenService.isValid(anyString())).thenReturn(false);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(accessTokenService).isValid("token");
    verify(userDetailsService, never()).loadUserByUsername(anyString());
  }

  @Test
  void doFilter_shouldFilterWithAlreadyPresentAuthentication_whenTokenIsValidButAuthIsPresent()
      throws ServletException, IOException {
    request.addHeader(AUTHORIZATION_HEADER, "Bearer token");
    when(accessTokenService.isValid(anyString())).thenReturn(true);
    var user = TestUserFactory.createLocalUser();
    var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    var context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(auth);
    SecurityContextHolder.setContext(context);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(accessTokenService).isValid("token");
    verify(userDetailsService, never()).loadUserByUsername(anyString());
  }

  @Test
  void doFilter_shouldFilterWithNewlyAddedAuthentication_whenTokenIsValidAndAuthIsNotPresentYet()
      throws ServletException, IOException {
    request.addHeader(AUTHORIZATION_HEADER, "Bearer token");
    when(accessTokenService.isValid(anyString())).thenReturn(true);
    var context = SecurityContextHolder.createEmptyContext();
    SecurityContextHolder.setContext(context);
    var email = "john.doe@example.com";
    when(accessTokenService.getEmailFromToken(anyString())).thenReturn(email);
    var user = TestUserFactory.createLocalUser(email);
    when(userDetailsService.loadUserByUsername(anyString())).thenReturn(user);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(accessTokenService).isValid("token");
    verify(accessTokenService).getEmailFromToken("token");
    verify(userDetailsService).loadUserByUsername(email);
    var auth = context.getAuthentication();
    assertNotNull(auth);
    assertTrue(auth.isAuthenticated());
    assertNull(auth.getCredentials());
    assertSame(user, auth.getPrincipal());
    assertEquals(user.getAuthorities(), auth.getAuthorities());
    assertInstanceOf(WebAuthenticationDetails.class, auth.getDetails());
  }
}
