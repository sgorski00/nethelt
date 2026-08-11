package pl.sgorski.nethelt.webapi.features.monitoring_task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.MonitoringTask;

public interface MonitoringTaskRepository extends JpaRepository<MonitoringTask, Long> {}
