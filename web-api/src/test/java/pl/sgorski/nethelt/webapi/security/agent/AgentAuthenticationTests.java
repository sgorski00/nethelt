package pl.sgorski.nethelt.webapi.security.agent;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Objects;
import org.junit.jupiter.api.Test;

public class AgentAuthenticationTests {

  @Test
  void constructor_shouldSetAuthenticated_whenCreated() {
    var principal = new AgentPrincipal(1L, 1L);

    var auth = new AgentAuthentication(principal);

    assertTrue(auth.isAuthenticated());
  }

  @Test
  void constructor_shouldAddAgentAuthority_whenCreated() {
    var principal = new AgentPrincipal(1L, 1L);

    var auth = new AgentAuthentication(principal);

    assertTrue(
        auth.getAuthorities().stream().anyMatch(a -> Objects.equals(a.getAuthority(), "AGENT")));
  }

  @Test
  void getCredentials_shouldReturnNull() {
    var principal = new AgentPrincipal(1L, 1L);

    var auth = new AgentAuthentication(principal);

    assertNull(auth.getCredentials());
  }

  @Test
  void getPrincipal_shouldReturnAgentPrincipal() {
    var principal = new AgentPrincipal(1L, 1L);

    var auth = new AgentAuthentication(principal);

    assertSame(principal, auth.getPrincipal());
  }
}
