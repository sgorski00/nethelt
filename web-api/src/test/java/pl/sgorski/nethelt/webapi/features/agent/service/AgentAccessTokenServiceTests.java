package pl.sgorski.nethelt.webapi.features.agent.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.features.auth.config.AuthProperties;
import pl.sgorski.nethelt.webapi.security.token.JwtService;
import pl.sgorski.nethelt.webapi.utils.TestAgentFactory;

@ExtendWith(MockitoExtension.class)
public class AgentAccessTokenServiceTests {

  @Mock private AuthProperties authProperties;
  @Mock private JwtService jwtService;
  @InjectMocks private AgentAccessTokenService agentAccessTokenService;

  @Test
  void generateAccessToken_shouldGenerateTokenWithCorrectValues() {
    when(authProperties.jwtTokenExpiration()).thenReturn(Duration.ofMinutes(15));
    when(jwtService.generate(any())).thenReturn("token");

    var result =
        agentAccessTokenService.generateAccessToken(TestAgentFactory.createAgentWithId(1L, 2L));

    assertEquals("token", result);
    verify(jwtService)
        .generate(
            argThat(
                payload ->
                    payload.expiration().equals(Duration.ofMinutes(15))
                        && payload.subject().equals("1")
                        && payload.claims().get("type").equals("AGENT")
                        && payload.claims().get("agentId").equals(1L)
                        && payload.claims().get("networkId").equals(2L)));
  }

  @Test
  void isValid_shouldReturnTrue_whenTokenIsValid() {
    var token = "token";
    when(jwtService.isValid(token)).thenReturn(true);

    var result = agentAccessTokenService.isValid(token);

    assertTrue(result);
  }

  @Test
  void isValid_shouldReturnFalse_whenTokenIsNotValid() {
    var token = "token";
    when(jwtService.isValid(token)).thenReturn(false);

    var result = agentAccessTokenService.isValid(token);

    assertFalse(result);
  }

  @Test
  void getAgentId_shouldReturnId_whenFound() {
    var token = "token";
    when(jwtService.getClaim(token, "agentId", Long.class)).thenReturn(1L);

    var result = agentAccessTokenService.getAgentId(token);

    assertEquals(1L, result);
  }

  @Test
  void getNetworkId_shouldReturnId_whenFound() {
    var token = "token";
    when(jwtService.getClaim(token, "networkId", Long.class)).thenReturn(1L);

    var result = agentAccessTokenService.getNetworkId(token);

    assertEquals(1L, result);
  }
}
