package pl.sgorski.nethelt.webapi.features.agent.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AgentTokenServiceTests {

  private AgentTokenService agentTokenService;

  @BeforeEach
  void setUp() {
    this.agentTokenService = new AgentTokenService();
  }

  @Test
  void generateRawToken_shouldGenerateNotNUllUUIDString() {
    var token = agentTokenService.generateRawToken();

    assertNotNull(token);
    assertDoesNotThrow(() -> UUID.fromString(token));
  }

  @Test
  void hashToken_shouldHashTokenAsASha256Hex() {
    var token = "test-token-123";

    var hashed = agentTokenService.hashToken(token);

    assertNotNull(hashed);
    assertEquals(64, hashed.length());
    assertEquals(DigestUtils.sha256Hex(token), hashed);
  }

  @Test
  void verifyToken_shouldReturnTrue_whenTokenIsEqual() {
    var token = "test-token-123";
    var hashed = DigestUtils.sha256Hex(token);

    var result = agentTokenService.verifyToken(token, hashed);

    assertTrue(result);
  }

  @Test
  void verifyToken_shouldReturnFalse_whenTokenIsNotEqual() {
    var hashed = DigestUtils.sha256Hex("test-token-123");

    var result = agentTokenService.verifyToken("some-other-token", hashed);

    assertFalse(result);
  }
}
