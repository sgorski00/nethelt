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
import pl.sgorski.nethelt.webapi.exception.domain.monitoring_task.MonitoringTaskValidationFailedException;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.MonitoringTaskConfiguration;

@Entity
@Getter
@Table(name = "http_healthcheck_task_configurations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HttpHealthcheckTaskConfiguration extends MonitoringTaskConfiguration {

  @Column(nullable = false)
  private int port;

  @Column(nullable = false)
  private String path;

  @Getter
  @Column(nullable = false)
  @JdbcTypeCode(SqlTypes.INTERVAL_SECOND)
  private Duration timeout;

  public HttpHealthcheckTaskConfiguration(int port, String path, Duration timeout) {
    if (port < 1 || port > 65535) {
      throw new MonitoringTaskValidationFailedException("TCP/IP port must be between 1 and 65535");
    }
    if (!path.startsWith("/")) {
      throw new MonitoringTaskValidationFailedException(
          "HTTP path must be non-empty and start with '/'");
    }
    this.port = port;
    this.path = path;
    this.timeout = timeout;
  }
}
