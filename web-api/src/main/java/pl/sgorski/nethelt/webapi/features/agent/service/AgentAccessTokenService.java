package pl.sgorski.nethelt.webapi.features.agent.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.sgorski.nethelt.webapi.features.agent.domain.Agent;
import pl.sgorski.nethelt.webapi.features.auth.config.AuthProperties;
import pl.sgorski.nethelt.webapi.security.token.JwtPayload;
import pl.sgorski.nethelt.webapi.security.token.JwtService;

@Service
@RequiredArgsConstructor
public class AgentAccessTokenService {

  private final AuthProperties authProperties;
  private final JwtService jwtService;

  public String generateAccessToken(Agent agent) {
    var subject = agent.getId().toString();
    var claims =
        Map.of("agentId", agent.getId(), "networkId", agent.getNetwork().getId(), "type", "AGENT");
    var payload = new JwtPayload(subject, claims, authProperties.jwtTokenExpiration());
    return jwtService.generate(payload);
  }

  public boolean isValid(String token) {
    return jwtService.isValid(token);
  }

  public Long getAgentId(String token) {
    return jwtService.getClaim(token, "agentId", Long.class);
  }

  public Long getNetworkId(String token) {
    return jwtService.getClaim(token, "networkId", Long.class);
  }
}
