package pl.sgorski.nethelt.webapi.features.monitoring_task.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.MonitoringTask;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.command.MonitoringTaskCreateCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.command.MonitoringTaskUpdateCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.request.MonitoringTaskCreateRequest;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.request.MonitoringTaskUpdateRequest;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.response.MonitoringTaskResponse;

@Mapper(componentModel = "spring")
public interface MonitoringTaskMapper {
  @Mapping(target = "isEnabled", source = "enabled")
  MonitoringTaskResponse toResponse(MonitoringTask monitoringTask);

  MonitoringTaskUpdateCommand toCommand(MonitoringTaskUpdateRequest request);

  MonitoringTaskCreateCommand toCommand(MonitoringTaskCreateRequest request);
}
