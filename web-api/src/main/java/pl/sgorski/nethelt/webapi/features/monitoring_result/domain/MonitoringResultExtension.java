package pl.sgorski.nethelt.webapi.features.monitoring_result.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "monitoring_result_extensions")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class MonitoringResultExtension {

  @Id
  @Getter
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
}
