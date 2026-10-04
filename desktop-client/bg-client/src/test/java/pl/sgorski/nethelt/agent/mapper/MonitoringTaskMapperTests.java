package pl.sgorski.nethelt.agent.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.sgorski.nethelt.agent.model.monitoring_task.HttpHealthcheckTaskConfiguration;
import pl.sgorski.nethelt.agent.model.monitoring_task.HttpScheme;
import pl.sgorski.nethelt.agent.model.monitoring_task.PingTaskConfiguration;
import pl.sgorski.nethelt.agent.model.monitoring_task.TaskType;
import pl.sgorski.nethelt.agent.model.monitoring_task.TelnetTaskConfiguration;
import pl.sgorski.nethelt.agent.webclient.dto.response.HttpHealthcheckTaskConfigurationResponse;
import pl.sgorski.nethelt.agent.webclient.dto.response.MonitoringTaskConfigurationResponse;
import pl.sgorski.nethelt.agent.webclient.dto.response.MonitoringTaskResponse;
import pl.sgorski.nethelt.agent.webclient.dto.response.PingTaskConfigurationResponse;
import pl.sgorski.nethelt.agent.webclient.dto.response.TelnetTaskConfigurationResponse;

public class MonitoringTaskMapperTests {

  private static final Instant UPDATED_AT = Instant.parse("2026-10-04T12:00:00Z");

  private MonitoringTaskMapper monitoringTaskMapper;

  @BeforeEach
  void setUp() {
    monitoringTaskMapper = new MonitoringTaskMapper();
  }

  @Test
  void toModel_shouldMapPingTask() {
    var response =
        createResponse(TaskType.PING, "192.168.1.10", new PingTaskConfigurationResponse(timeout()));

    var task = monitoringTaskMapper.toModel(response);

    assertEquals(1L, task.id());
    assertEquals(2L, task.deviceId());
    assertEquals(InetAddress.ofLiteral("192.168.1.10"), task.deviceIp());
    assertEquals(TaskType.PING, task.type());
    assertEquals(Duration.ofSeconds(30), task.interval());
    assertTrue(task.enabled());
    assertEquals(UPDATED_AT, task.updatedAt());
    assertEquals(new PingTaskConfiguration(timeout()), task.configuration());
  }

  @Test
  void toModel_shouldMapTelnetTask() {
    var response =
        createResponse(
            TaskType.TELNET, "10.0.0.1", new TelnetTaskConfigurationResponse(22, timeout()));

    var task = monitoringTaskMapper.toModel(response);

    assertEquals(TaskType.TELNET, task.type());
    assertEquals(new TelnetTaskConfiguration(22, timeout()), task.configuration());
  }

  @Test
  void toModel_shouldMapHttpHealthcheckTask() {
    var response =
        createResponse(
            TaskType.HTTP_HEALTHCHECK,
            "10.0.0.1",
            new HttpHealthcheckTaskConfigurationResponse(
                HttpScheme.HTTPS, 443, "/health", "example.com", 204, timeout()));

    var task = monitoringTaskMapper.toModel(response);

    assertEquals(TaskType.HTTP_HEALTHCHECK, task.type());
    assertEquals(
        new HttpHealthcheckTaskConfiguration(
            HttpScheme.HTTPS, 443, "/health", "example.com", 204, timeout()),
        task.configuration());
  }

  @Test
  void toModel_shouldMapIpv6Address() {
    var response =
        createResponse(TaskType.PING, "::1", new PingTaskConfigurationResponse(timeout()));

    var task = monitoringTaskMapper.toModel(response);

    assertEquals(InetAddress.ofLiteral("::1"), task.deviceIp());
  }

  @Test
  void toModel_shouldThrow_whenDeviceIpIsNotIpLiteral() {
    var response =
        createResponse(TaskType.PING, "example.com", new PingTaskConfigurationResponse(timeout()));

    assertThrows(IllegalArgumentException.class, () -> monitoringTaskMapper.toModel(response));
  }

  private static MonitoringTaskResponse createResponse(
      TaskType type, String deviceIp, MonitoringTaskConfigurationResponse configuration) {
    return new MonitoringTaskResponse(
        1L, type, 2L, deviceIp, Duration.ofSeconds(30), true, configuration, UPDATED_AT);
  }

  private static Duration timeout() {
    return Duration.ofSeconds(5);
  }
}
