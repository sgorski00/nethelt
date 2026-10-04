package pl.sgorski.nethelt.webapi.features.monitoring_result.domain.extension;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.MonitoringResultExtension;

@Entity
@Table(name = "http_healthcheck_result_extensions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HttpHealthcheckResultExtension extends MonitoringResultExtension {

  @Getter @Column @Nullable private Integer statusCode;

  public HttpHealthcheckResultExtension(@Nullable Integer statusCode) {
    super();
    this.statusCode = statusCode;
  }
}
