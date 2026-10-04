package pl.sgorski.nethelt.webapi.features.monitoring_result.domain.extension;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.MonitoringResultExtension;

@Entity
@Table(name = "ping_result_extensions")
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class PingResultExtension extends MonitoringResultExtension {}
