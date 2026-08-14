package pl.sgorski.nethelt.webapi.features.monitoring_task.domain;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.Instant;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import pl.sgorski.nethelt.webapi.features.device.domain.Device;

@Entity
@Getter
@Table(name = "monitoring_tasks")
@EqualsAndHashCode(exclude = {"device", "configuration"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonitoringTask {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "device_id", nullable = false)
  private Device device;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TaskType type;

  @Column(nullable = false)
  @JdbcTypeCode(SqlTypes.INTERVAL_SECOND)
  private Duration interval;

  @Column(nullable = false)
  private boolean isEnabled = true;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "configuration_id")
  private MonitoringTaskConfiguration configuration;

  @CreationTimestamp
  @Column(nullable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private Instant updatedAt;

  public MonitoringTask(
      Device device, TaskType type, Duration interval, MonitoringTaskConfiguration configuration) {
    this.device = device;
    this.type = type;
    this.interval = interval;
    this.configuration = configuration;
  }

  public void update(Duration interval) {
    this.interval = interval;
  }

  public void enable() {
    this.isEnabled = true;
  }

  public void disable() {
    this.isEnabled = false;
  }
}
