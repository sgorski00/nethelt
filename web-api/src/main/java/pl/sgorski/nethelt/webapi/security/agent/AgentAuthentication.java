package pl.sgorski.nethelt.webapi.security.agent;

import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public final class AgentAuthentication extends AbstractAuthenticationToken {

  private final AgentPrincipal agent;

  public AgentAuthentication(AgentPrincipal agent) {
    super(List.of(new SimpleGrantedAuthority("AGENT")));
    this.agent = agent;
    setAuthenticated(true);
  }

  @Override
  public @Nullable Object getCredentials() {
    return null;
  }

  @Override
  public AgentPrincipal getPrincipal() {
    return agent;
  }
}
