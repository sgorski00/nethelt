package pl.sgorski.nethelt.webapi.security.agent;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.sgorski.nethelt.webapi.features.agent.repository.AgentRepository;
import pl.sgorski.nethelt.webapi.features.agent.service.AgentAccessTokenService;

@Component
@RequiredArgsConstructor
public final class AgentAccessTokenAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Agent ";

  private final AgentAccessTokenService accessTokenService;
  private final AgentRepository agentRepository;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    var header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header == null || !header.startsWith(BEARER_PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    var token = header.substring(BEARER_PREFIX.length());
    if (!accessTokenService.isValid(token)) {
      filterChain.doFilter(request, response);
      return;
    }

    var securityContext = SecurityContextHolder.getContext();
    if (securityContext.getAuthentication() == null) {
      var agentId = accessTokenService.getAgentId(token);
      var agent = agentRepository.findById(agentId).orElse(null);
      if (agent == null || !agent.isActive()) {
        filterChain.doFilter(request, response);
        return;
      }

      var principal = new AgentPrincipal(agentId, agent.getNetwork().getId());
      var authentication = new AgentAuthentication(principal);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    filterChain.doFilter(request, response);
  }
}
