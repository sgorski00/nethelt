package pl.sgorski.nethelt.webapi.features.monitoring_task.domain.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pl.sgorski.nethelt.webapi.exception.domain.monitoring_task.MonitoringTaskValidationFailedException;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.HttpScheme;

public class HttpHealthcheckTaskConfigurationTests {

  @Test
  void constructor_shouldCreateValidConfiguration() {
    var config =
        new HttpHealthcheckTaskConfiguration(
            HttpScheme.HTTPS, 8080, "/health", "example.com", 204, Duration.ofSeconds(5));

    assertEquals(HttpScheme.HTTPS, config.getScheme());
    assertEquals(8080, config.getPort());
    assertEquals("/health", config.getPath());
    assertEquals("example.com", config.getHost());
    assertEquals(204, config.getExpectedStatusCode());
    assertEquals(Duration.ofSeconds(5), config.getTimeout());
  }

  @Test
  void constructor_shouldCreateValidConfiguration_whenHostIsNull() {
    var config =
        new HttpHealthcheckTaskConfiguration(
            HttpScheme.HTTP, 8080, "/health", null, null, Duration.ofSeconds(5));

    assertNull(config.getHost());
    assertNull(config.getExpectedStatusCode());
  }

  @Test
  void constructor_shouldThrow_whenPortIsNotPositive() {
    assertThrows(
        MonitoringTaskValidationFailedException.class,
        () ->
            new HttpHealthcheckTaskConfiguration(
                HttpScheme.HTTP, 0, "/health", null, null, Duration.ofSeconds(5)));
  }

  @Test
  void constructor_shouldThrow_whenPortIsTooBig() {
    assertThrows(
        MonitoringTaskValidationFailedException.class,
        () ->
            new HttpHealthcheckTaskConfiguration(
                HttpScheme.HTTP, 65536, "/health", null, null, Duration.ofSeconds(5)));
  }

  @Test
  void constructor_shouldThrow_whenPathNotStartsWithBackslash() {
    assertThrows(
        MonitoringTaskValidationFailedException.class,
        () ->
            new HttpHealthcheckTaskConfiguration(
                HttpScheme.HTTP, 8080, "health", null, null, Duration.ofSeconds(5)));
  }

  @Test
  void constructor_shouldThrow_whenHostIsBlank() {
    assertThrows(
        MonitoringTaskValidationFailedException.class,
        () ->
            new HttpHealthcheckTaskConfiguration(
                HttpScheme.HTTP, 8080, "/health", "  ", null, Duration.ofSeconds(5)));
  }

  @ParameterizedTest
  @ValueSource(ints = {99, 600})
  void constructor_shouldThrow_whenExpectedStatusCodeIsOutOfRange(int expectedStatusCode) {
    assertThrows(
        MonitoringTaskValidationFailedException.class,
        () ->
            new HttpHealthcheckTaskConfiguration(
                HttpScheme.HTTP, 8080, "/health", null, expectedStatusCode, Duration.ofSeconds(5)));
  }
}
