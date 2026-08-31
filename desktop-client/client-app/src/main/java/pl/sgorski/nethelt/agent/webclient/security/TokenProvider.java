package pl.sgorski.nethelt.agent.webclient.security;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.security.storage.CredentialsStore;
import pl.sgorski.nethelt.agent.webclient.api.auth.AgentAuthClient;
import pl.sgorski.nethelt.agent.webclient.dto.request.AgentAuthRequest;

@Component
@RequiredArgsConstructor
public final class TokenProvider {

  private final CredentialsStore credentialsStore;
  private final AgentAuthClient agentClient;

  public Optional<String> getToken() {
    return credentialsStore
        .get()
        .map(s -> agentClient.authenticate(new AgentAuthRequest(s)).token());
  }
}
