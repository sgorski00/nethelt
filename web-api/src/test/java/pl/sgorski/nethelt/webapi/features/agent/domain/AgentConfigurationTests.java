package pl.sgorski.nethelt.webapi.features.agent.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.webapi.exception.domain.agent.AgentValidationFailedException;

public class AgentConfigurationTests {

  @Test
  void defaults_shouldUseDefaultMaxUnavailabilityTime() {
    var configuration = AgentConfiguration.defaults();

    assertEquals(
        AgentConfiguration.DEFAULT_MAX_UNAVAILABILITY_TIME,
        configuration.getMaxUnavailabilityTime());
  }

  @Test
  void constructor_shouldSetMaxUnavailabilityTime() {
    var configuration = new AgentConfiguration(Duration.ofMinutes(10));

    assertEquals(Duration.ofMinutes(10), configuration.getMaxUnavailabilityTime());
  }

  @Test
  void constructor_shouldThrow_whenMaxUnavailabilityTimeIsZero() {
    assertThrows(AgentValidationFailedException.class, () -> new AgentConfiguration(Duration.ZERO));
  }

  @Test
  void constructor_shouldThrow_whenMaxUnavailabilityTimeIsNegative() {
    assertThrows(
        AgentValidationFailedException.class, () -> new AgentConfiguration(Duration.ofSeconds(-1)));
  }
}
