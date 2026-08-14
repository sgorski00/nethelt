package pl.sgorski.nethelt.webapi.features.monitoring_task.domain.configuration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Duration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.MonitoringTaskConfiguration;

@Entity
@Table(name = "ping_task_configurations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PingTaskConfiguration extends MonitoringTaskConfiguration {

  @Getter
  @Column(nullable = false)
  @JdbcTypeCode(SqlTypes.INTERVAL_SECOND)
  private Duration timeout;

  public PingTaskConfiguration(Duration timeout) {
    this.timeout = timeout;
  }
}
