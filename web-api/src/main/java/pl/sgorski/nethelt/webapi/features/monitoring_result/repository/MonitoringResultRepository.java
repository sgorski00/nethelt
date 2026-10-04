package pl.sgorski.nethelt.webapi.features.monitoring_result.repository;

import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.MonitoringResult;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.MonitoringTask;

public interface MonitoringResultRepository extends JpaRepository<MonitoringResult, Long> {
  Optional<MonitoringResult> findByTaskAndId(MonitoringTask task, Long id);

  Set<MonitoringResult> findAllByTask(MonitoringTask task);
}
