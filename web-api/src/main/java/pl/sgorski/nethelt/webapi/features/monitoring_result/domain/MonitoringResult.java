package pl.sgorski.nethelt.webapi.features.monitoring_result.domain;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.MonitoringTask;

@Entity
@Getter
@Table(name = "monitoring_results")
@EqualsAndHashCode(exclude = {"task", "extension"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonitoringResult {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "task_id", nullable = false)
  private MonitoringTask task;

  @Column(nullable = false)
  private Instant executedAt;

  @Column(nullable = false)
  private boolean success;

  @Column @Nullable private String message;

  @Column @Nullable private Long responseTimeMs;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
  @JoinColumn(name = "monitoring_result_extension_id", nullable = false, unique = true)
  private MonitoringResultExtension extension;

  @CreationTimestamp
  @Column(nullable = false)
  private Instant createdAt;

  public MonitoringResult(
      MonitoringTask task,
      Instant executedAt,
      boolean success,
      @Nullable String message,
      @Nullable Long responseTimeMs,
      MonitoringResultExtension extension) {
    this.task = task;
    this.executedAt = executedAt;
    this.success = success;
    this.message = message;
    this.responseTimeMs = responseTimeMs;
    this.extension = extension;
  }
}
