package pl.sgorski.nethelt.webapi.features.monitoring_task.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.MonitoringTask;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.command.MonitoringTaskConfigurationCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.command.MonitoringTaskCreateCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.command.MonitoringTaskUpdateCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.command.configuration.HttpHealthcheckTaskConfigurationCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.command.configuration.PingTaskConfigurationCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.command.configuration.TelnetTaskConfigurationCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.request.MonitoringTaskConfigurationRequest;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.request.MonitoringTaskCreateRequest;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.request.MonitoringTaskUpdateRequest;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.request.configuration.HttpHealthcheckTaskConfigurationRequest;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.request.configuration.PingTaskConfigurationRequest;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.request.configuration.TelnetTaskConfigurationRequest;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.response.MonitoringTaskResponse;

@Mapper(componentModel = "spring")
public interface MonitoringTaskMapper {
  @Mapping(target = "isEnabled", source = "enabled")
  MonitoringTaskResponse toResponse(MonitoringTask monitoringTask);

  MonitoringTaskUpdateCommand toCommand(MonitoringTaskUpdateRequest request);

  MonitoringTaskCreateCommand toCommand(MonitoringTaskCreateRequest request);

  PingTaskConfigurationCommand toCommand(PingTaskConfigurationRequest request);

  TelnetTaskConfigurationCommand toCommand(TelnetTaskConfigurationRequest request);

  HttpHealthcheckTaskConfigurationCommand toCommand(
      HttpHealthcheckTaskConfigurationRequest request);

  default MonitoringTaskConfigurationCommand toCommand(MonitoringTaskConfigurationRequest request) {
    return switch (request) {
      case PingTaskConfigurationRequest ping -> toCommand(ping);
      case TelnetTaskConfigurationRequest telnet -> toCommand(telnet);
      case HttpHealthcheckTaskConfigurationRequest http -> toCommand(http);
      default -> throw new IllegalStateException("Unconvertible type: " + request);
    };
  }
}
