package pl.sgorski.nethelt.webapi.features.metrics.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.features.metrics.domain.breakdown.TaskMetrics;
import pl.sgorski.nethelt.webapi.features.metrics.domain.stats.BucketStats;
import pl.sgorski.nethelt.webapi.features.metrics.domain.stats.TypeStats;
import pl.sgorski.nethelt.webapi.features.metrics.repository.MetricsRepository;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.TaskType;

@ExtendWith(MockitoExtension.class)
public class MetricsServiceTests {

  private static final Instant FROM = Instant.parse("2026-10-10T10:00:00Z");
  private static final Instant TO = Instant.parse("2026-10-10T11:00:00Z");

  @Mock private MetricsRepository metricsRepository;
  @InjectMocks private MetricsService metricsService;

  @ParameterizedTest
  @CsvSource({"PT1H, PT30S", "PT24H, PT15M", "P7D, PT2H", "P31D, PT12H", "P365D, P1D"})
  void chooseBucket_shouldKeepChartsAroundTargetPoints(Duration length, Duration expected) {
    assertEquals(expected, MetricsService.chooseBucket(TO.minus(length), TO));
  }

  @Test
  void getMetrics_shouldPassFiltersToRepository() {
    metricsService.getMetrics(1L, 2L, 3L, TaskType.PING, FROM, TO);

    verify(metricsRepository).findStatsByType(1L, 2L, 3L, TaskType.PING, FROM, TO);
    verify(metricsRepository).findStatsByBucketAndType(1L, 2L, 3L, TaskType.PING, FROM, TO, 30L);
    verify(metricsRepository)
        .findStatsByTask(1L, 2L, 3L, TaskType.PING, FROM, TO, MetricsService.BREAKDOWN_LIMIT);
  }

  @Test
  void getMetrics_shouldReturnEmptyMetrics_whenNoResults() {
    var metrics = metricsService.getMetrics(1L, null, null, null, FROM, TO);

    assertSame(FROM, metrics.from());
    assertSame(TO, metrics.to());
    assertEquals(0, metrics.summary().total());
    assertNull(metrics.summary().uptimePercent());
    assertTrue(metrics.summary().latency().isEmpty());
    assertTrue(metrics.availability().isEmpty());
    assertTrue(metrics.latency().isEmpty());
    assertTrue(metrics.breakdown().isEmpty());
  }

  @Test
  void getMetrics_shouldAggregateResults() {
    var bucket = FROM.getEpochSecond() / 30;
    var worstTask =
        new TaskMetrics(2L, "Server", 4L, TaskType.HTTP_HEALTHCHECK, 2L, 0L, 0.0, 300.0);
    when(metricsRepository.findStatsByType(1L, null, null, null, FROM, TO))
        .thenReturn(
            List.of(
                new TypeStats(TaskType.PING, 6L, 6L, 10.0, 12.0),
                new TypeStats(TaskType.HTTP_HEALTHCHECK, 2L, 0L, 300.0, 400.0)));
    when(metricsRepository.findStatsByBucketAndType(1L, null, null, null, FROM, TO, 30L))
        .thenReturn(
            List.of(
                new BucketStats(bucket, TaskType.PING, 3L, 3L, 10.0, 11.0),
                new BucketStats(bucket, TaskType.HTTP_HEALTHCHECK, 2L, 0L, 300.0, 400.0),
                new BucketStats(bucket + 1, TaskType.PING, 3L, 3L, 10.0, 12.0)));
    when(metricsRepository.findStatsByTask(anyLong(), any(), any(), any(), any(), any(), anyInt()))
        .thenReturn(List.of(worstTask));

    var metrics = metricsService.getMetrics(1L, null, null, null, FROM, TO);

    var summary = metrics.summary();
    assertEquals(8, summary.total());
    assertEquals(6, summary.successful());
    assertEquals(2, summary.failed());
    assertEquals(75.0, summary.uptimePercent());
    assertEquals(2, summary.latency().size());

    assertEquals(Duration.ofSeconds(30), metrics.bucket());
    assertEquals(2, metrics.availability().size());
    var first = metrics.availability().getFirst();
    assertEquals(FROM, first.bucketStart());
    assertEquals(5, first.total());
    assertEquals(3, first.successful());
    assertEquals(FROM.plusSeconds(30), metrics.availability().get(1).bucketStart());

    assertEquals(3, metrics.latency().size());
    assertEquals(TaskType.HTTP_HEALTHCHECK, metrics.latency().get(1).type());

    assertEquals(List.of(worstTask), metrics.breakdown());
  }
}
