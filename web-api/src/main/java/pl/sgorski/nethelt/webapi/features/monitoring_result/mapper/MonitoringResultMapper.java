package pl.sgorski.nethelt.webapi.features.monitoring_result.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.MonitoringResult;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.MonitoringResultExtension;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.extension.HttpHealthcheckResultExtension;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.extension.PingResultExtension;
import pl.sgorski.nethelt.webapi.features.monitoring_result.domain.extension.TelnetResultExtension;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.command.MonitoringResultAddCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.command.MonitoringResultExtensionCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.command.extension.HttpHealthcheckResultExtensionCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.command.extension.PingResultExtensionCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.command.extension.TelnetResultExtensionCommand;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.request.MonitoringResultAddRequest;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.request.MonitoringResultExtensionRequest;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.request.extension.HttpHealthcheckResultExtensionRequest;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.request.extension.PingResultExtensionRequest;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.request.extension.TelnetResultExtensionRequest;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.response.MonitoringResultExtensionResponse;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.response.MonitoringResultResponse;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.response.extension.HttpHealthcheckResultExtensionResponse;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.response.extension.PingResultExtensionResponse;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.response.extension.TelnetResultExtensionResponse;

@Mapper(componentModel = "spring")
public interface MonitoringResultMapper {
  @Mapping(target = "type", source = "task.type")
  @Mapping(target = "extension", source = "extension")
  MonitoringResultResponse toResponse(MonitoringResult monitoringResult);

  MonitoringResultAddCommand toCommand(MonitoringResultAddRequest request);

  PingResultExtensionCommand toCommand(PingResultExtensionRequest request);

  TelnetResultExtensionCommand toCommand(TelnetResultExtensionRequest request);

  HttpHealthcheckResultExtensionCommand toCommand(HttpHealthcheckResultExtensionRequest request);

  default MonitoringResultExtensionCommand toCommand(MonitoringResultExtensionRequest request) {
    return switch (request) {
      case PingResultExtensionRequest ping -> toCommand(ping);
      case TelnetResultExtensionRequest telnet -> toCommand(telnet);
      case HttpHealthcheckResultExtensionRequest http -> toCommand(http);
      default -> throw new IllegalStateException("Unconvertible type: " + request);
    };
  }

  PingResultExtensionResponse toResponse(PingResultExtension extension);

  TelnetResultExtensionResponse toResponse(TelnetResultExtension extension);

  HttpHealthcheckResultExtensionResponse toResponse(HttpHealthcheckResultExtension extension);

  default MonitoringResultExtensionResponse toExtensionResponse(
      MonitoringResultExtension extension) {
    return switch (extension) {
      case PingResultExtension ping -> toResponse(ping);
      case TelnetResultExtension telnet -> toResponse(telnet);
      case HttpHealthcheckResultExtension http -> toResponse(http);
      default -> throw new IllegalStateException("Unconvertible type: " + extension);
    };
  }
}
