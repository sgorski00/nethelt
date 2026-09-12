package pl.sgorski.nethelt.webapi.features.monitoring_task.repository;

import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.sgorski.nethelt.webapi.features.device.domain.Device;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.MonitoringTask;

public interface MonitoringTaskRepository extends JpaRepository<MonitoringTask, Long> {
  Set<MonitoringTask> findAllByDevice(Device device);

  Optional<MonitoringTask> findByDeviceAndId(Device device, Long monitoringTaskId);

  @Query(
"""
    select mt from MonitoringTask mt
    where mt.device.network.id = :networkId and mt.isEnabled = true
""")
  Set<MonitoringTask> findAllActiveByNetworkId(@Param("networkId") Long networkId);
}
