package pl.sgorski.nethelt.webapi.features.monitoring_result.repository;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityManager;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.config.PostgresIntegrationTest;
import pl.sgorski.nethelt.webapi.features.device.domain.DeviceType;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.MonitoringResult;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.MonitoringResultKey;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.extension.PingResultExtension;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.MonitoringTask;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.TaskType;
import pl.sgorski.nethelt.webapi.utils.TestDeviceFactory;
import pl.sgorski.nethelt.webapi.utils.TestMonitoringTaskFactory;
import pl.sgorski.nethelt.webapi.utils.TestNetworkFactory;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;

@Transactional
public class MonitoringResultRepositoryTests extends PostgresIntegrationTest {

  private static final Instant EXECUTED_AT = Instant.parse("2026-11-04T12:00:00.123456Z");

  @Autowired private MonitoringResultRepository monitoringResultRepository;
  @Autowired private EntityManager entityManager;

  private MonitoringTask ping;
  private MonitoringTask otherPing;

  @BeforeEach
  void setUp() {
    var user = TestUserFactory.createLocalUser("results@example.com");
    var network = TestNetworkFactory.createNetwork(user, "Results", null);
    var router =
        TestDeviceFactory.createDevice(network, "Router", "10.0.0.1", DeviceType.LAN_CLIENT);
    ping = TestMonitoringTaskFactory.createTask(router, TaskType.PING, Duration.ofSeconds(30));
    otherPing = TestMonitoringTaskFactory.createTask(router, TaskType.PING, Duration.ofSeconds(60));
    persist(user, network, router, ping, otherPing);
    persist(result(ping, EXECUTED_AT), result(otherPing, EXECUTED_AT.plusSeconds(60)));
    entityManager.flush();
    entityManager.clear();
  }

  @Test
  void findByTaskAndExecutedAt_shouldFindResult_withMicrosecondPrecision() {
    var found = monitoringResultRepository.findByTaskAndExecutedAt(ping, EXECUTED_AT);

    assertTrue(found.isPresent());
    assertEquals(EXECUTED_AT, found.get().getExecutedAt());
  }

  @Test
  void findKeysByTaskIdInAndExecutedAtIn_shouldReturnOnlyMatchingKeys() {
    var keys =
        monitoringResultRepository.findKeysByTaskIdInAndExecutedAtIn(
            Set.of(ping.getId(), otherPing.getId()),
            List.of(EXECUTED_AT, EXECUTED_AT.plusSeconds(1)));

    assertEquals(List.of(new MonitoringResultKey(ping.getId(), EXECUTED_AT)), keys);
  }

  @Test
  void save_shouldFail_whenResultOfTaskWithSameExecutedAtExists() {
    var task = entityManager.find(MonitoringTask.class, ping.getId());

    assertThrows(
        DataIntegrityViolationException.class,
        () -> monitoringResultRepository.saveAndFlush(result(task, EXECUTED_AT)));
  }

  @Test
  void save_shouldAllowSameExecutedAt_forDifferentTasks() {
    var task = entityManager.find(MonitoringTask.class, otherPing.getId());

    assertDoesNotThrow(() -> monitoringResultRepository.saveAndFlush(result(task, EXECUTED_AT)));
  }

  private static MonitoringResult result(MonitoringTask task, Instant executedAt) {
    return new MonitoringResult(task, executedAt, true, null, 10L, new PingResultExtension());
  }

  private void persist(Object... entities) {
    for (var entity : entities) {
      entityManager.persist(entity);
    }
  }
}
