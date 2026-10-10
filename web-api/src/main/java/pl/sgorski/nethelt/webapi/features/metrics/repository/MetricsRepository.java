package pl.sgorski.nethelt.webapi.features.metrics.repository;

import java.time.Instant;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import pl.sgorski.nethelt.webapi.features.metrics.domain.breakdown.TaskMetrics;
import pl.sgorski.nethelt.webapi.features.metrics.domain.stats.BucketStats;
import pl.sgorski.nethelt.webapi.features.metrics.domain.stats.TypeStats;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.MonitoringResult;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.TaskType;

/**
 * Read-only aggregations over the monitoring results of one network, optionally narrowed to a
 * device, a task or a task type.
 */
public interface MetricsRepository extends Repository<MonitoringResult, Long> {

  String SCOPE =
      """
      FROM MonitoringResult r
      JOIN r.task t
      JOIN t.device d
      WHERE d.network.id = :networkId
        AND (:deviceId IS NULL OR d.id = :deviceId)
        AND (:taskId IS NULL OR t.id = :taskId)
        AND (:type IS NULL OR t.type = :type)
        AND r.executedAt >= :from
        AND r.executedAt < :to
      """;

  @Query(
      """
      SELECT new pl.sgorski.nethelt.webapi.features.metrics.domain.stats.TypeStats(
               t.type,
               COUNT(r),
               CAST(SUM(CASE WHEN r.success THEN 1 ELSE 0 END) AS Long),
               CAST(AVG(r.responseTimeMs) AS Double),
               CAST(PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY r.responseTimeMs) AS Double))
      """
          + SCOPE
          + """
      GROUP BY t.type
      """)
  List<TypeStats> findStatsByType(
      @Param("networkId") Long networkId,
      @Param("deviceId") @Nullable Long deviceId,
      @Param("taskId") @Nullable Long taskId,
      @Param("type") @Nullable TaskType type,
      @Param("from") Instant from,
      @Param("to") Instant to);

  @Query(
      """
      SELECT new pl.sgorski.nethelt.webapi.features.metrics.domain.stats.BucketStats(
               CAST(FLOOR(EXTRACT(EPOCH FROM r.executedAt) / :bucketSeconds) AS Long) AS bucket,
               t.type,
               COUNT(r),
               CAST(SUM(CASE WHEN r.success THEN 1 ELSE 0 END) AS Long),
               CAST(AVG(r.responseTimeMs) AS Double),
               CAST(PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY r.responseTimeMs) AS Double))
      """
          + SCOPE
          + """
      GROUP BY bucket, t.type
      ORDER BY bucket
      """)
  List<BucketStats> findStatsByBucketAndType(
      @Param("networkId") Long networkId,
      @Param("deviceId") @Nullable Long deviceId,
      @Param("taskId") @Nullable Long taskId,
      @Param("type") @Nullable TaskType type,
      @Param("from") Instant from,
      @Param("to") Instant to,
      @Param("bucketSeconds") long bucketSeconds);

  /** Tasks with the lowest uptime first, then the ones with the most results. */
  @Query(
      """
      SELECT new pl.sgorski.nethelt.webapi.features.metrics.domain.breakdown.TaskMetrics(
               d.id,
               d.name,
               t.id,
               t.type,
               COUNT(r),
               CAST(SUM(CASE WHEN r.success THEN 1 ELSE 0 END) AS Long),
               CAST(SUM(CASE WHEN r.success THEN 1 ELSE 0 END) * 100.0 / COUNT(r) AS Double) AS uptime,
               CAST(AVG(r.responseTimeMs) AS Double))
      """
          + SCOPE
          + """
      GROUP BY d.id, d.name, t.id, t.type
      ORDER BY uptime, COUNT(r) DESC, t.id
      LIMIT :limit
      """)
  List<TaskMetrics> findStatsByTask(
      @Param("networkId") Long networkId,
      @Param("deviceId") @Nullable Long deviceId,
      @Param("taskId") @Nullable Long taskId,
      @Param("type") @Nullable TaskType type,
      @Param("from") Instant from,
      @Param("to") Instant to,
      @Param("limit") int limit);
}
