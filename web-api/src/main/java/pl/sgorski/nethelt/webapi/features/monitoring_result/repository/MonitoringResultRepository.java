package pl.sgorski.nethelt.webapi.features.monitoring_result.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.MonitoringResult;

public interface MonitoringResultRepository extends JpaRepository<MonitoringResult, Long> {}
