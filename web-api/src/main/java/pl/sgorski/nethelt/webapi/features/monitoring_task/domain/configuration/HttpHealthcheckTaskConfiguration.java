package pl.sgorski.nethelt.webapi.features.monitoring_task.domain.configuration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Duration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.exception.domain.monitoring_task.MonitoringTaskValidationFailedException;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.HttpScheme;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.MonitoringTaskConfiguration;

@Entity
@Getter
@Table(name = "http_healthcheck_task_configurations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HttpHealthcheckTaskConfiguration extends MonitoringTaskConfiguration {

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private HttpScheme scheme;

  @Column(nullable = false)
  private int port;

  @Column(nullable = false)
  private String path;

  @Column private @Nullable String host;

  @Column private @Nullable Integer expectedStatusCode;

  @Getter
  @Column(nullable = false)
  @JdbcTypeCode(SqlTypes.INTERVAL_SECOND)
  private Duration timeout;

  public HttpHealthcheckTaskConfiguration(
      HttpScheme scheme,
      int port,
      String path,
      @Nullable String host,
      @Nullable Integer expectedStatusCode,
      Duration timeout) {
    if (port < 1 || port > 65535) {
      throw new MonitoringTaskValidationFailedException("TCP/IP port must be between 1 and 65535");
    }
    if (!path.startsWith("/")) {
      throw new MonitoringTaskValidationFailedException(
          "HTTP path must be non-empty and start with '/'");
    }
    if (host != null && host.isBlank()) {
      throw new MonitoringTaskValidationFailedException(
          "HTTP host must be non-blank when provided");
    }
    if (expectedStatusCode != null && (expectedStatusCode < 100 || expectedStatusCode > 599)) {
      throw new MonitoringTaskValidationFailedException(
          "Expected HTTP status code must be between 100 and 599");
    }
    this.scheme = scheme;
    this.port = port;
    this.path = path;
    this.host = host;
    this.expectedStatusCode = expectedStatusCode;
    this.timeout = timeout;
  }
}
