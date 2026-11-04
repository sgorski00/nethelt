package pl.sgorski.nethelt.webapi.features.monitoring_result.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.MonitoringResult;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.MonitoringResultKey;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.MonitoringTask;

public interface MonitoringResultRepository extends JpaRepository<MonitoringResult, Long> {
  Optional<MonitoringResult> findByTaskAndId(MonitoringTask task, Long id);

  Optional<MonitoringResult> findByTaskAndExecutedAt(MonitoringTask task, Instant executedAt);

  @Query(
"""
        SELECT new pl.sgorski.nethelt.webapi.features.monitoring_result.domain.MonitoringResultKey(
            r.task.id, r.executedAt)
        FROM MonitoringResult r
        WHERE r.task.id IN :taskIds
          AND r.executedAt IN :executedAts
""")
  List<MonitoringResultKey> findKeysByTaskIdInAndExecutedAtIn(
      @Param("taskIds") Collection<Long> taskIds,
      @Param("executedAts") Collection<Instant> executedAts);

  @Query(
"""
        SELECT r
        FROM MonitoringResult r
        WHERE r.task = :task
          AND r.executedAt >= COALESCE(:from, r.executedAt)
          AND r.executedAt <= COALESCE(:to, r.executedAt)
        ORDER BY r.executedAt DESC
""")
  Page<MonitoringResult> findAllByTaskAndExecutedAtBetween(
      @Param("task") MonitoringTask task,
      @Nullable @Param("from") Instant from,
      @Nullable @Param("to") Instant to,
      Pageable pageable);
}
