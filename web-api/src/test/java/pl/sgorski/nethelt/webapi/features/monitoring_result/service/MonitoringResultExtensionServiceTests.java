package pl.sgorski.nethelt.webapi.features.monitoring_result.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.webapi.exception.domain.monitoring_result.MonitoringResultValidationFailedException;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.extension.HttpHealthcheckResultExtension;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.extension.PingResultExtension;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.extension.TelnetResultExtension;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.command.extension.HttpHealthcheckResultExtensionCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.command.extension.PingResultExtensionCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.command.extension.TelnetResultExtensionCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.TaskType;

public class MonitoringResultExtensionServiceTests {

  private MonitoringResultExtensionService monitoringResultExtensionService;

  @BeforeEach
  void setUp() {
    monitoringResultExtensionService = new MonitoringResultExtensionService();
  }

  @Test
  void mapToExtension_shouldCreatePingResultExtension() {
    var type = TaskType.PING;
    var extensionCommand = new PingResultExtensionCommand();

    var result = monitoringResultExtensionService.mapToExtension(type, extensionCommand);

    assertInstanceOf(PingResultExtension.class, result);
  }

  @Test
  void mapToExtension_shouldCreateTelnetResultExtension() {
    var type = TaskType.TELNET;
    var extensionCommand = new TelnetResultExtensionCommand(true);

    var result = monitoringResultExtensionService.mapToExtension(type, extensionCommand);

    assertInstanceOf(TelnetResultExtension.class, result);
    var telnetExtension = (TelnetResultExtension) result;
    assertTrue(telnetExtension.isPortOpen());
  }

  @Test
  void mapToExtension_shouldCreateHttpHealthcheckResultExtension() {
    var type = TaskType.HTTP_HEALTHCHECK;
    var extensionCommand = new HttpHealthcheckResultExtensionCommand(503);

    var result = monitoringResultExtensionService.mapToExtension(type, extensionCommand);

    assertInstanceOf(HttpHealthcheckResultExtension.class, result);
    var healthcheckExtension = (HttpHealthcheckResultExtension) result;
    assertEquals(503, healthcheckExtension.getStatusCode());
  }

  @Test
  void mapToExtension_shouldCreateHttpHealthcheckResultExtension_whenStatusCodeIsNull() {
    var type = TaskType.HTTP_HEALTHCHECK;
    var extensionCommand = new HttpHealthcheckResultExtensionCommand(null);

    var result = monitoringResultExtensionService.mapToExtension(type, extensionCommand);

    assertInstanceOf(HttpHealthcheckResultExtension.class, result);
    var healthcheckExtension = (HttpHealthcheckResultExtension) result;
    assertNull(healthcheckExtension.getStatusCode());
  }

  @Test
  void mapToExtension_shouldThrow_whenExtensionNotMatch_Ping() {
    var type = TaskType.PING;
    var extensionCommand = new TelnetResultExtensionCommand(true);

    assertThrows(
        MonitoringResultValidationFailedException.class,
        () -> monitoringResultExtensionService.mapToExtension(type, extensionCommand));
  }

  @Test
  void mapToExtension_shouldThrow_whenExtensionNotMatch_Telnet() {
    var type = TaskType.TELNET;
    var extensionCommand = new HttpHealthcheckResultExtensionCommand(200);

    assertThrows(
        MonitoringResultValidationFailedException.class,
        () -> monitoringResultExtensionService.mapToExtension(type, extensionCommand));
  }

  @Test
  void mapToExtension_shouldThrow_whenExtensionNotMatch_HttpHealthcheck() {
    var type = TaskType.HTTP_HEALTHCHECK;
    var extensionCommand = new PingResultExtensionCommand();

    assertThrows(
        MonitoringResultValidationFailedException.class,
        () -> monitoringResultExtensionService.mapToExtension(type, extensionCommand));
  }
}
