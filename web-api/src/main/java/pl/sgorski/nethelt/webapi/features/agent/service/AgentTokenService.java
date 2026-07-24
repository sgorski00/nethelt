package pl.sgorski.nethelt.webapi.features.agent.service;

import java.util.UUID;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

@Component
public class AgentTokenService {

  public String generateRawToken() {
    return UUID.randomUUID().toString();
  }

  public String hashToken(String token) {
    return DigestUtils.sha256Hex(token);
  }

  public boolean verifyToken(String rawToken, String hashedToken) {
    return hashToken(rawToken).equals(hashedToken);
  }
}
