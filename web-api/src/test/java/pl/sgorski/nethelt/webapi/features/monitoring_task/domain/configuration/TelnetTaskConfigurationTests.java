package pl.sgorski.nethelt.webapi.features.monitoring_task.domain.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.webapi.exception.domain.monitoring_task.MonitoringTaskValidationFailedException;

public class TelnetTaskConfigurationTests {

  @Test
  void constructor_shouldCreateValidConfiguration() {
    var config = new TelnetTaskConfiguration(8080, Duration.ofSeconds(5));

    assertEquals(8080, config.getPort());
    assertEquals(Duration.ofSeconds(5), config.getTimeout());
  }

  @Test
  void constructor_shouldThrow_whenPortIsNotPositive() {
    assertThrows(
        MonitoringTaskValidationFailedException.class,
        () -> new TelnetTaskConfiguration(0, Duration.ofSeconds(5)));
  }

  @Test
  void constructor_shouldThrow_whenPortIsTooBig() {
    assertThrows(
        MonitoringTaskValidationFailedException.class,
        () -> new TelnetTaskConfiguration(65536, Duration.ofSeconds(5)));
  }
}
