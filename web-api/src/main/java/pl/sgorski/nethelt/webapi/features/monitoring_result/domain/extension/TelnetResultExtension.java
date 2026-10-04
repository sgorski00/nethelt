package pl.sgorski.nethelt.webapi.features.monitoring_result.domain.extension;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.MonitoringResultExtension;

@Entity
@Table(name = "telnet_result_extensions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TelnetResultExtension extends MonitoringResultExtension {

  @Getter
  @Column(nullable = false)
  private boolean portOpen;

  public TelnetResultExtension(boolean portOpen) {
    super();
    this.portOpen = portOpen;
  }
}
