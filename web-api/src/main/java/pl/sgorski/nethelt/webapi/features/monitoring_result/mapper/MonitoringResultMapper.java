package pl.sgorski.nethelt.webapi.features.monitoring_result.mapper;

import org.mapstruct.Mapper;
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

@Mapper(componentModel = "spring")
public interface MonitoringResultMapper {
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
}
