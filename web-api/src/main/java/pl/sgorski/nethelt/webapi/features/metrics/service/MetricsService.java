package pl.sgorski.nethelt.webapi.features.metrics.service;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import pl.sgorski.nethelt.webapi.features.metrics.domain.Metrics;
import pl.sgorski.nethelt.webapi.features.metrics.domain.series.AvailabilityPoint;
import pl.sgorski.nethelt.webapi.features.metrics.domain.series.LatencyPoint;
import pl.sgorski.nethelt.webapi.features.metrics.domain.stats.BucketStats;
import pl.sgorski.nethelt.webapi.features.metrics.domain.stats.TypeStats;
import pl.sgorski.nethelt.webapi.features.metrics.domain.summary.LatencyStats;
import pl.sgorski.nethelt.webapi.features.metrics.domain.summary.MetricsSummary;
import pl.sgorski.nethelt.webapi.features.metrics.repository.MetricsRepository;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.TaskType;

@Service
@RequiredArgsConstructor
public class MetricsService {

  static final int BREAKDOWN_LIMIT = 10;

  /** Roughly how many points a chart should get, whatever the range. */
  private static final int TARGET_POINTS = 120;

  private static final List<Duration> BUCKETS =
      List.of(
          Duration.ofSeconds(30),
          Duration.ofMinutes(1),
          Duration.ofMinutes(2),
          Duration.ofMinutes(5),
          Duration.ofMinutes(10),
          Duration.ofMinutes(15),
          Duration.ofMinutes(30),
          Duration.ofHours(1),
          Duration.ofHours(2),
          Duration.ofHours(3),
          Duration.ofHours(6),
          Duration.ofHours(12),
          Duration.ofDays(1));

  private final MetricsRepository metricsRepository;

  /**
   * Aggregates the results of a network in the given range. The scope is narrowed by any
   * combination of device, task and task type. A device or task from outside of the network is not
   * rejected, it simply matches no results.
   */
  public Metrics getMetrics(
      Long networkId,
      @Nullable Long deviceId,
      @Nullable Long taskId,
      @Nullable TaskType type,
      Instant from,
      Instant to) {
    var bucket = chooseBucket(from, to);

    var byType = metricsRepository.findStatsByType(networkId, deviceId, taskId, type, from, to);
    var byBucket =
        metricsRepository.findStatsByBucketAndType(
            networkId, deviceId, taskId, type, from, to, bucket.toSeconds());
    var byTask =
        metricsRepository.findStatsByTask(
            networkId, deviceId, taskId, type, from, to, BREAKDOWN_LIMIT);

    return new Metrics(
        from,
        to,
        bucket,
        toSummary(byType),
        toAvailability(byBucket, bucket),
        toLatency(byBucket, bucket),
        byTask);
  }

  /** Picks the smallest bucket that keeps the chart at most around {@link #TARGET_POINTS}. */
  static Duration chooseBucket(Instant from, Instant to) {
    var minimal = Duration.between(from, to).dividedBy(TARGET_POINTS);
    return BUCKETS.stream()
        .filter(bucket -> bucket.compareTo(minimal) >= 0)
        .findFirst()
        .orElse(BUCKETS.getLast());
  }

  private static MetricsSummary toSummary(List<TypeStats> rows) {
    var total = rows.stream().mapToLong(TypeStats::total).sum();
    var successful = rows.stream().mapToLong(TypeStats::successful).sum();
    var uptimePercent = total > 0 ? successful * 100.0 / total : null;
    var latency =
        rows.stream()
            .map(row -> new LatencyStats(row.type(), row.total(), row.avgMs(), row.p95Ms()))
            .toList();
    return new MetricsSummary(total, successful, total - successful, uptimePercent, latency);
  }

  /** Availability does not depend on the task type, so the rows of one bucket are summed. */
  private static List<AvailabilityPoint> toAvailability(List<BucketStats> rows, Duration bucket) {
    var points = new LinkedHashMap<Instant, AvailabilityPoint>();
    for (var row : rows) {
      var point = new AvailabilityPoint(bucketStart(row, bucket), row.total(), row.successful());
      points.merge(
          point.bucketStart(),
          point,
          (a, b) ->
              new AvailabilityPoint(
                  a.bucketStart(), a.total() + b.total(), a.successful() + b.successful()));
    }
    return List.copyOf(points.values());
  }

  private static List<LatencyPoint> toLatency(List<BucketStats> rows, Duration bucket) {
    return rows.stream()
        .map(
            row -> new LatencyPoint(bucketStart(row, bucket), row.type(), row.avgMs(), row.p95Ms()))
        .toList();
  }

  private static Instant bucketStart(BucketStats row, Duration bucket) {
    return Instant.ofEpochSecond(row.bucket() * bucket.toSeconds());
  }
}
