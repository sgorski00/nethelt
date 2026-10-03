package pl.sgorski.nethelt.agent.test_utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import pl.sgorski.nethelt.agent.model.monitoring_task.HttpHealthcheckTaskConfiguration;
import pl.sgorski.nethelt.agent.model.monitoring_task.HttpScheme;
import pl.sgorski.nethelt.agent.model.monitoring_task.MonitoringTask;
import pl.sgorski.nethelt.agent.model.monitoring_task.PingTaskConfiguration;
import pl.sgorski.nethelt.agent.model.monitoring_task.TaskType;
import pl.sgorski.nethelt.agent.model.monitoring_task.TelnetTaskConfiguration;

public class TestMonitoringTaskFactory {

  public static MonitoringTask createTelnetMonitoringTask(Long id) {
    var config = createTelnetTaskConfiguration();
    var interval = Duration.ofSeconds(30);
    return new MonitoringTask(
        id, 1L, getLocalhost(), TaskType.TELNET, interval, true, config, Instant.now());
  }

  public static MonitoringTask createPingMonitoringTask(Long id) {
    return createPingMonitoringTask(id, getLocalhost());
  }

  public static MonitoringTask createPingMonitoringTask(Long id, InetAddress address) {
    var config = createPingTaskConfiguration();
    var interval = Duration.ofSeconds(30);
    return new MonitoringTask(
        id, 1L, address, TaskType.PING, interval, true, config, Instant.now());
  }

  public static MonitoringTask createHttpHealthcheckMonitoringTask(
      Long id, InetAddress address, HttpHealthcheckTaskConfiguration config) {
    var interval = Duration.ofSeconds(30);
    return new MonitoringTask(
        id, 1L, address, TaskType.HTTP_HEALTHCHECK, interval, true, config, Instant.now());
  }

  public static HttpHealthcheckTaskConfiguration createHttpHealthcheckTaskConfiguration(
      int port, String path, String host, Duration timeout) {
    return createHttpHealthcheckTaskConfiguration(port, path, host, null, timeout);
  }

  public static HttpHealthcheckTaskConfiguration createHttpHealthcheckTaskConfiguration(
      int port, String path, String host, Integer expectedStatusCode, Duration timeout) {
    return new HttpHealthcheckTaskConfiguration(
        HttpScheme.HTTP, port, path, host, expectedStatusCode, timeout);
  }

  private static TelnetTaskConfiguration createTelnetTaskConfiguration() {
    return new TelnetTaskConfiguration(80, Duration.ofSeconds(5));
  }

  private static PingTaskConfiguration createPingTaskConfiguration() {
    return new PingTaskConfiguration(Duration.ofSeconds(5));
  }

  private static InetAddress getLocalhost() {
    try {
      return InetAddress.getLocalHost();
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }
}
