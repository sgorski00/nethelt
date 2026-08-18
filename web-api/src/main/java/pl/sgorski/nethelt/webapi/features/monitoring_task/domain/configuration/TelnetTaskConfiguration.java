package pl.sgorski.nethelt.webapi.features.monitoring_task.domain.configuration;

import jakarta.persistence.*;
import java.time.Duration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pl.sgorski.nethelt.webapi.exception.domain.monitoring_task.MonitoringTaskValidationFailedException;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.MonitoringTaskConfiguration;

@Entity
@Table(name = "telnet_task_configurations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TelnetTaskConfiguration extends MonitoringTaskConfiguration {

  @Getter
  @Column(nullable = false)
  private int port;

  @Getter
  @Column(nullable = false)
  @JdbcTypeCode(SqlTypes.INTERVAL_SECOND)
  private Duration timeout;

  public TelnetTaskConfiguration(int port, Duration timeout) {
    if (port < 1 || port > 65535) {
      throw new MonitoringTaskValidationFailedException("TCP/IP port must be between 1 and 65535");
    }
    this.port = port;
    this.timeout = timeout;
  }
}
