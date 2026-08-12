package pl.sgorski.nethelt.webapi.features.monitoring_task.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.MonitoringTask;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.response.MonitoringTaskResponse;

@Mapper(componentModel = "spring")
public interface MonitoringTaskMapper {
  @Mapping(target = "isEnabled", source = "enabled")
  MonitoringTaskResponse toResponse(MonitoringTask monitoringTask);
}
