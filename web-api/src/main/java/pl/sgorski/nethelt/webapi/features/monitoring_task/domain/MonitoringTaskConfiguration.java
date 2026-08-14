package pl.sgorski.nethelt.webapi.features.monitoring_task.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "monitoring_task_configurations")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class MonitoringTaskConfiguration {

  @Id
  @Getter
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
}
