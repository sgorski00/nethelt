package pl.sgorski.nethelt.webapi.features.discovery.mapper;

import java.util.Collection;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.sgorski.nethelt.webapi.features.device.domain.Device;
import pl.sgorski.nethelt.webapi.features.device.domain.Ipv4Range;
import pl.sgorski.nethelt.webapi.features.discovery.domain.DiscoveryScan;
import pl.sgorski.nethelt.webapi.features.discovery.domain.DiscoveryScanResult;
import pl.sgorski.nethelt.webapi.features.discovery.dto.command.DiscoveryScanAckCommand;
import pl.sgorski.nethelt.webapi.features.discovery.dto.command.DiscoveryScanCompleteCommand;
import pl.sgorski.nethelt.webapi.features.discovery.dto.command.DiscoveryScanStartCommand;
import pl.sgorski.nethelt.webapi.features.discovery.dto.request.DiscoveryScanAckRequest;
import pl.sgorski.nethelt.webapi.features.discovery.dto.request.DiscoveryScanResultsRequest;
import pl.sgorski.nethelt.webapi.features.discovery.dto.request.DiscoveryScanStartRequest;
import pl.sgorski.nethelt.webapi.features.discovery.dto.response.DiscoveryScanClientResponse;
import pl.sgorski.nethelt.webapi.features.discovery.dto.response.DiscoveryScanResponse;
import pl.sgorski.nethelt.webapi.features.discovery.dto.response.DiscoveryScanResultResponse;

@Mapper(componentModel = "spring")
public interface DiscoveryScanMapper {
  DiscoveryScanStartCommand toCommand(Long networkId, DiscoveryScanStartRequest request);

  DiscoveryScanAckCommand toCommand(Long networkId, DiscoveryScanAckRequest request);

  DiscoveryScanCompleteCommand toCommand(Long networkId, DiscoveryScanResultsRequest request);

  @Mapping(target = "rangeStart", qualifiedByName = "ipLiteral")
  @Mapping(target = "rangeEnd", qualifiedByName = "ipLiteral")
  DiscoveryScanResponse toResponse(DiscoveryScan scan, @Context Collection<Device> networkDevices);

  @Mapping(target = "rangeStart", qualifiedByName = "ipLiteral")
  @Mapping(target = "rangeEnd", qualifiedByName = "ipLiteral")
  DiscoveryScanClientResponse toClientResponse(DiscoveryScan scan);

  default DiscoveryScanResultResponse toResponse(
      DiscoveryScanResult result, @Context Collection<Device> networkDevices) {
    var existingDeviceId =
        networkDevices.stream()
            .filter(device -> device.getIpAddress().equals(result.getIpAddress()))
            .map(Device::getId)
            .findFirst()
            .orElse(null);
    return new DiscoveryScanResultResponse(
        result.getIpAddress().getHostAddress(),
        result.getHostname(),
        result.getResponseTimeMs(),
        existingDeviceId);
  }

  @Named("ipLiteral")
  default @Nullable String toIpLiteral(@Nullable Long address) {
    return address == null ? null : Ipv4Range.toLiteral(address);
  }
}
