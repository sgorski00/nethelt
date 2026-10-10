package pl.sgorski.nethelt.webapi.features.metrics.repository;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityManager;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.config.PostgresIntegrationTest;
import pl.sgorski.nethelt.webapi.features.device.domain.Device;
import pl.sgorski.nethelt.webapi.features.device.domain.DeviceType;
import pl.sgorski.nethelt.webapi.features.metrics.domain.stats.TypeStats;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.MonitoringResult;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.extension.HttpHealthcheckResultExtension;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.extension.PingResultExtension;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.MonitoringTask;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.TaskType;
import pl.sgorski.nethelt.webapi.features.network.domain.Network;
import pl.sgorski.nethelt.webapi.utils.TestDeviceFactory;
import pl.sgorski.nethelt.webapi.utils.TestMonitoringTaskFactory;
import pl.sgorski.nethelt.webapi.utils.TestNetworkFactory;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;

@Transactional
public class MetricsRepositoryTests extends PostgresIntegrationTest {

  private static final Instant FROM = Instant.parse("2026-10-10T10:00:00Z");
  private static final Instant TO = Instant.parse("2026-10-10T11:00:00Z");

  @Autowired private MetricsRepository metricsRepository;
  @Autowired private EntityManager entityManager;

  private Network network;
  private Device router;
  private MonitoringTask ping;
  private MonitoringTask http;

  @BeforeEach
  void setUp() {
    var user = TestUserFactory.createLocalUser("metrics@example.com");
    network = TestNetworkFactory.createNetwork(user, "Metrics", null);
    router = TestDeviceFactory.createDevice(network, "Router", "10.0.0.1", DeviceType.LAN_CLIENT);
    var server =
        TestDeviceFactory.createDevice(network, "Server", "10.0.0.2", DeviceType.LAN_CLIENT);
    ping = TestMonitoringTaskFactory.createTask(router, TaskType.PING, Duration.ofSeconds(30));
    http =
        TestMonitoringTaskFactory.createTask(
            server, TaskType.HTTP_HEALTHCHECK, Duration.ofSeconds(30));
    persist(user, network, router, server, ping, http);

    persist(pingResult(FROM.plusSeconds(10), true, 10L));
    persist(pingResult(FROM.plusSeconds(40), true, 20L));
    persist(pingResult(FROM.plusSeconds(70), false, null));
    persist(httpResult(FROM.plusSeconds(20), false, 300L));
    persist(httpResult(FROM.plusSeconds(80), true, 100L));
    // outside of the range
    persist(pingResult(TO, true, 999L));
    persist(pingResult(FROM.minusSeconds(1), true, 999L));
    entityManager.flush();
  }

  @Test
  void findStatsByType_shouldAggregatePerType_whenNoFilters() {
    var stats =
        metricsRepository.findStatsByType(network.getId(), null, null, null, FROM, TO).stream()
            .sorted(Comparator.comparing(TypeStats::type))
            .toList();

    assertEquals(2, stats.size());
    var pingStats = stats.get(0);
    assertEquals(TaskType.PING, pingStats.type());
    assertEquals(3, pingStats.total());
    assertEquals(2, pingStats.successful());
    assertEquals(15.0, pingStats.avgMs(), 0.001);
    assertEquals(19.5, pingStats.p95Ms(), 0.001);
    var httpStats = stats.get(1);
    assertEquals(TaskType.HTTP_HEALTHCHECK, httpStats.type());
    assertEquals(2, httpStats.total());
    assertEquals(1, httpStats.successful());
    assertEquals(200.0, httpStats.avgMs(), 0.001);
  }

  @Test
  void findStatsByType_shouldNarrowScope_whenFiltersGiven() {
    var byDevice =
        metricsRepository.findStatsByType(network.getId(), router.getId(), null, null, FROM, TO);
    var byTask =
        metricsRepository.findStatsByType(network.getId(), null, http.getId(), null, FROM, TO);
    var byType =
        metricsRepository.findStatsByType(network.getId(), null, null, TaskType.PING, FROM, TO);
    var otherNetwork =
        metricsRepository.findStatsByType(network.getId() + 1, null, null, null, FROM, TO);

    assertEquals(TaskType.PING, byDevice.getFirst().type());
    assertEquals(1, byDevice.size());
    assertEquals(TaskType.HTTP_HEALTHCHECK, byTask.getFirst().type());
    assertEquals(1, byTask.size());
    assertEquals(TaskType.PING, byType.getFirst().type());
    assertEquals(1, byType.size());
    assertTrue(otherNetwork.isEmpty());
  }

  @Test
  void findStatsByBucketAndType_shouldGroupByBucket() {
    var stats =
        metricsRepository.findStatsByBucketAndType(
            network.getId(), null, null, TaskType.PING, FROM, TO, 60);

    assertEquals(2, stats.size());
    assertEquals(FROM.getEpochSecond() / 60, stats.get(0).bucket());
    assertEquals(2, stats.get(0).total());
    assertEquals(FROM.getEpochSecond() / 60 + 1, stats.get(1).bucket());
    assertEquals(0, stats.get(1).successful());
    assertNull(stats.get(1).avgMs());
  }

  @Test
  void findStatsByTask_shouldReturnWorstTasksFirst() {
    var stats = metricsRepository.findStatsByTask(network.getId(), null, null, null, FROM, TO, 1);

    assertEquals(1, stats.size());
    var task = stats.getFirst();
    assertEquals(http.getId(), task.taskId());
    assertEquals("Server", task.deviceName());
    assertEquals(50.0, task.uptimePercent(), 0.001);
  }

  private MonitoringResult pingResult(
      Instant executedAt, boolean success, @Nullable Long responseTimeMs) {
    return new MonitoringResult(
        ping, executedAt, success, null, responseTimeMs, new PingResultExtension());
  }

  private MonitoringResult httpResult(Instant executedAt, boolean success, Long responseTimeMs) {
    return new MonitoringResult(
        http,
        executedAt,
        success,
        null,
        responseTimeMs,
        new HttpHealthcheckResultExtension(success ? 200 : 500));
  }

  private void persist(Object... entities) {
    for (var entity : entities) {
      entityManager.persist(entity);
    }
  }
}
