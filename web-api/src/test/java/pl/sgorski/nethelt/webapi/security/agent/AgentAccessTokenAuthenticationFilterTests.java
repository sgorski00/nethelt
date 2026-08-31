package pl.sgorski.nethelt.webapi.security.agent;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.sgorski.nethelt.webapi.features.agent.repository.AgentRepository;
import pl.sgorski.nethelt.webapi.features.agent.service.AgentAccessTokenService;
import pl.sgorski.nethelt.webapi.utils.TestAgentFactory;

@ExtendWith(MockitoExtension.class)
public class AgentAccessTokenAuthenticationFilterTests {

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  @Mock private FilterChain filterChain;
  @Mock private AgentAccessTokenService accessTokenService;
  @Mock private AgentRepository agentRepository;
  @InjectMocks private AgentAccessTokenAuthenticationFilter filter;

  @BeforeEach
  void setUp() {
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
  }

  @Test
  void doFilter_shouldFilterWithoutAuthentication_whenAuthorizationHeaderNotFound()
      throws ServletException, IOException {
    request.removeHeader(HttpHeaders.AUTHORIZATION);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(accessTokenService, never()).isValid(anyString());
    verify(agentRepository, never()).findById(anyLong());
  }

  @Test
  void doFilter_shouldFilterWithoutAuthentication_whenAuthorizationIsNotAgent()
      throws ServletException, IOException {
    request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token");

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(accessTokenService, never()).isValid(anyString());
    verify(agentRepository, never()).findById(anyLong());
  }

  @Test
  void doFilter_shouldFilterWithoutAuthentication_whenTokenIsNotValid()
      throws ServletException, IOException {
    request.addHeader(HttpHeaders.AUTHORIZATION, "Agent token");
    when(accessTokenService.isValid(eq("token"))).thenReturn(false);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(accessTokenService).isValid("token");
    verify(agentRepository, never()).findById(anyLong());
  }

  @Test
  void doFilter_shouldFilterWithAlreadyPresentAuthentication_whenTokenIsValidButAuthIsPresent()
      throws ServletException, IOException {
    request.addHeader(HttpHeaders.AUTHORIZATION, "Agent token");
    when(accessTokenService.isValid(eq("token"))).thenReturn(true);
    var agentPrincipal = new AgentPrincipal(1L, 1L);
    var auth = new AgentAuthentication(agentPrincipal);
    var context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(auth);
    SecurityContextHolder.setContext(context);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(accessTokenService).isValid("token");
    verify(agentRepository, never()).findById(anyLong());
  }

  @Test
  void doFilter_shouldFilterWithoutAuthentication_whenAgentIsNotActive()
      throws ServletException, IOException {
    request.addHeader(HttpHeaders.AUTHORIZATION, "Agent token");
    when(accessTokenService.isValid(eq("token"))).thenReturn(true);
    var context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(null);
    SecurityContextHolder.setContext(context);
    when(accessTokenService.getAgentId(eq("token"))).thenReturn(1L);
    var agent = TestAgentFactory.createAgent();
    agent.deactivate();
    when(agentRepository.findById(eq(1L))).thenReturn(Optional.of(agent));

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(accessTokenService).isValid("token");
    verify(agentRepository).findById(1L);
    assertNull(context.getAuthentication());
  }

  @Test
  void doFilter_shouldFilterWithoutAuthentication_whenAgentIsNotFound()
      throws ServletException, IOException {
    request.addHeader(HttpHeaders.AUTHORIZATION, "Agent token");
    when(accessTokenService.isValid(eq("token"))).thenReturn(true);
    var context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(null);
    SecurityContextHolder.setContext(context);
    when(accessTokenService.getAgentId(eq("token"))).thenReturn(1L);
    when(agentRepository.findById(eq(1L))).thenReturn(Optional.empty());

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(accessTokenService).isValid("token");
    verify(agentRepository).findById(1L);
    assertNull(context.getAuthentication());
  }

  @Test
  void doFilter_shouldFilterWithNewlyAddedAuthentication_whenTokenIsValidAndAuthIsNotPresentYet()
      throws ServletException, IOException {
    request.addHeader(HttpHeaders.AUTHORIZATION, "Agent token");
    when(accessTokenService.isValid(eq("token"))).thenReturn(true);
    var context = SecurityContextHolder.createEmptyContext();
    SecurityContextHolder.setContext(context);
    when(accessTokenService.getAgentId(eq("token"))).thenReturn(1L);
    var agent = TestAgentFactory.createAgent();
    when(agentRepository.findById(eq(1L))).thenReturn(Optional.of(agent));

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(accessTokenService).isValid("token");
    var auth = context.getAuthentication();
    assertNotNull(auth);
    assertInstanceOf(AgentAuthentication.class, auth);
    assertInstanceOf(AgentPrincipal.class, auth.getPrincipal());
  }
}
