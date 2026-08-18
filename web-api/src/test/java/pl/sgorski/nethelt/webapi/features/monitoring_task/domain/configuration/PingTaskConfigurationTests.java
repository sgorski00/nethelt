package pl.sgorski.nethelt.webapi.features.monitoring_task.domain.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import org.junit.jupiter.api.Test;

public class PingTaskConfigurationTests {

  @Test
  void constructor_shouldCreateValidConfiguration() {
    var config = new PingTaskConfiguration(Duration.ofSeconds(3));

    assertEquals(Duration.ofSeconds(3), config.getTimeout());
  }
}
