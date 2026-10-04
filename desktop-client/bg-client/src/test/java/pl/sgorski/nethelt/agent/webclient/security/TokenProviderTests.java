package pl.sgorski.nethelt.agent.webclient.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.agent.exception.WebClientException;
import pl.sgorski.nethelt.agent.security.storage.CredentialsStore;
import pl.sgorski.nethelt.agent.webclient.api.auth.AgentAuthClient;
import pl.sgorski.nethelt.agent.webclient.dto.request.AgentAuthRequest;
import pl.sgorski.nethelt.agent.webclient.dto.response.AgentAuthResponse;

@ExtendWith(MockitoExtension.class)
public class TokenProviderTests {

  @Mock private CredentialsStore credentialsStore;
  @Mock private AgentAuthClient agentAuthClient;
  @InjectMocks private TokenProvider tokenProvider;

  @Test
  void getToken_shouldExchangePatForAgentToken() {
    when(credentialsStore.get()).thenReturn(Optional.of("pat"));
    when(agentAuthClient.authenticate(new AgentAuthRequest("pat")))
        .thenReturn(new AgentAuthResponse("agent-token"));

    var result = tokenProvider.getToken();

    assertEquals(Optional.of("agent-token"), result);
  }

  @Test
  void getToken_shouldReturnEmpty_whenPatIsNotRegistered() {
    when(credentialsStore.get()).thenReturn(Optional.empty());

    var result = tokenProvider.getToken();

    assertTrue(result.isEmpty());
    verifyNoInteractions(agentAuthClient);
  }

  @Test
  void getToken_shouldPropagateException_whenAuthenticationFails() {
    when(credentialsStore.get()).thenReturn(Optional.of("pat"));
    when(agentAuthClient.authenticate(new AgentAuthRequest("pat")))
        .thenThrow(new WebClientException("Web API request failed with status 401"));

    assertThrows(WebClientException.class, () -> tokenProvider.getToken());
  }
}
