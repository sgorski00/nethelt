package pl.sgorski.nethelt.webapi.features.monitoring_task.domain.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.webapi.exception.domain.monitoring_task.MonitoringTaskValidationFailedException;

public class HttpHealthcheckTaskConfigurationTests {

  @Test
  void constructor_shouldCreateValidConfiguration() {
    var config =
        new HttpHealthcheckTaskConfiguration(8080, "/health", java.time.Duration.ofSeconds(5));

    assertEquals(8080, config.getPort());
    assertEquals("/health", config.getPath());
    assertEquals(Duration.ofSeconds(5), config.getTimeout());
  }

  @Test
  void constructor_shouldThrow_whenPortIsNotPositive() {
    assertThrows(
        MonitoringTaskValidationFailedException.class,
        () -> new HttpHealthcheckTaskConfiguration(0, "/health", java.time.Duration.ofSeconds(5)));
  }

  @Test
  void constructor_shouldThrow_whenPortIsTooBig() {
    assertThrows(
        MonitoringTaskValidationFailedException.class,
        () ->
            new HttpHealthcheckTaskConfiguration(
                65536, "/health", java.time.Duration.ofSeconds(5)));
  }

  @Test
  void constructor_shouldThrow_whenPathNotStartsWithBackslash() {
    assertThrows(
        MonitoringTaskValidationFailedException.class,
        () ->
            new HttpHealthcheckTaskConfiguration(8080, "health", java.time.Duration.ofSeconds(5)));
  }
}
