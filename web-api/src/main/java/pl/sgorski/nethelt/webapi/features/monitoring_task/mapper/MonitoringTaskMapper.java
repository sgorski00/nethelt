package pl.sgorski.nethelt.webapi.features.monitoring_task.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.MonitoringTask;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.MonitoringTaskConfiguration;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.configuration.HttpHealthcheckTaskConfiguration;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.configuration.PingTaskConfiguration;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.configuration.TelnetTaskConfiguration;
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
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.response.MonitoringTaskConfigurationResponse;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.response.MonitoringTaskResponse;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.response.configuration.HttpHealthcheckTaskConfigurationResponse;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.response.configuration.PingTaskConfigurationResponse;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.response.configuration.TelnetTaskConfigurationResponse;

@Mapper(componentModel = "spring")
public interface MonitoringTaskMapper {
  @Mapping(target = "isEnabled", source = "enabled")
  @Mapping(target = "configuration", source = "configuration")
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

  PingTaskConfigurationResponse toResponse(PingTaskConfiguration configuration);

  TelnetTaskConfigurationResponse toResponse(TelnetTaskConfiguration configuration);

  HttpHealthcheckTaskConfigurationResponse toResponse(
      HttpHealthcheckTaskConfiguration configuration);

  default MonitoringTaskConfigurationResponse toConfigurationResponse(
      MonitoringTaskConfiguration configuration) {
    return switch (configuration) {
      case PingTaskConfiguration ping -> toResponse(ping);
      case TelnetTaskConfiguration telnet -> toResponse(telnet);
      case HttpHealthcheckTaskConfiguration http -> toResponse(http);
      default -> throw new IllegalStateException("Unconvertible type: " + configuration);
    };
  }
}
