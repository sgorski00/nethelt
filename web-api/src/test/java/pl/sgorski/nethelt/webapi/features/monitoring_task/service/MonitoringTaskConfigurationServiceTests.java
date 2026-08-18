package pl.sgorski.nethelt.webapi.features.monitoring_task.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.webapi.exception.domain.monitoring_task.MonitoringTaskValidationFailedException;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.TaskType;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.configuration.HttpHealthcheckTaskConfiguration;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.configuration.PingTaskConfiguration;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.configuration.TelnetTaskConfiguration;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.command.configuration.HttpHealthcheckTaskConfigurationCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.command.configuration.PingTaskConfigurationCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.command.configuration.TelnetTaskConfigurationCommand;

public class MonitoringTaskConfigurationServiceTests {

  private MonitoringTaskConfigurationService monitoringTaskConfigurationService;

  @BeforeEach
  void setUp() {
    monitoringTaskConfigurationService = new MonitoringTaskConfigurationService();
  }

  @Test
  void createConfiguration_shouldCreatePingTaskConfiguration() {
    var type = TaskType.PING;
    var configCommand = new PingTaskConfigurationCommand(1000L);

    var result = monitoringTaskConfigurationService.createConfiguration(type, configCommand);

    assertInstanceOf(PingTaskConfiguration.class, result);
    var pingConfig = (PingTaskConfiguration) result;
    assertEquals(Duration.ofSeconds(1), pingConfig.getTimeout());
  }

  @Test
  void createConfiguration_shouldCreateTelnetTaskConfiguration() {
    var type = TaskType.TELNET;
    var configCommand = new TelnetTaskConfigurationCommand(8080, 1000L);

    var result = monitoringTaskConfigurationService.createConfiguration(type, configCommand);

    assertInstanceOf(TelnetTaskConfiguration.class, result);
    var telnetConfig = (TelnetTaskConfiguration) result;
    assertEquals(Duration.ofSeconds(1), telnetConfig.getTimeout());
    assertEquals(8080, telnetConfig.getPort());
  }

  @Test
  void createConfiguration_shouldCreateHttpHealthcheckTaskConfiguration() {
    var type = TaskType.HTTP_HEALTHCHECK;
    var configCommand = new HttpHealthcheckTaskConfigurationCommand(8080, "/health", 1000L);

    var result = monitoringTaskConfigurationService.createConfiguration(type, configCommand);

    assertInstanceOf(HttpHealthcheckTaskConfiguration.class, result);
    var healthcheckConfig = (HttpHealthcheckTaskConfiguration) result;
    assertEquals(Duration.ofSeconds(1), healthcheckConfig.getTimeout());
    assertEquals(8080, healthcheckConfig.getPort());
    assertEquals("/health", healthcheckConfig.getPath());
  }

  @Test
  void createConfiguration_shouldThrow_whenConfigurationNotMatch_Ping() {
    var type = TaskType.PING;
    var configCommand = new HttpHealthcheckTaskConfigurationCommand(8080, "/health", 1000L);

    assertThrows(
        MonitoringTaskValidationFailedException.class,
        () -> monitoringTaskConfigurationService.createConfiguration(type, configCommand));
  }

  @Test
  void createConfiguration_shouldThrow_whenConfigurationNotMatch_Telnet() {
    var type = TaskType.TELNET;
    var configCommand = new PingTaskConfigurationCommand(1000L);

    assertThrows(
        MonitoringTaskValidationFailedException.class,
        () -> monitoringTaskConfigurationService.createConfiguration(type, configCommand));
  }

  @Test
  void createConfiguration_shouldThrow_whenConfigurationNotMatch_HttpHealthcheck() {
    var type = TaskType.HTTP_HEALTHCHECK;
    var configCommand = new TelnetTaskConfigurationCommand(8080, 1000L);

    assertThrows(
        MonitoringTaskValidationFailedException.class,
        () -> monitoringTaskConfigurationService.createConfiguration(type, configCommand));
  }

  @Test
  void createConfiguration_shouldThrow_whenTimeoutIsNotPositive() {
    var type = TaskType.PING;
    var configCommand = new PingTaskConfigurationCommand(0);

    assertThrows(
        MonitoringTaskValidationFailedException.class,
        () -> monitoringTaskConfigurationService.createConfiguration(type, configCommand));
  }
}
