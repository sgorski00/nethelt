package pl.sgorski.nethelt.webapi.features.device.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.sgorski.nethelt.webapi.features.device.domain.Device;
import pl.sgorski.nethelt.webapi.features.device.dto.command.DeviceCreateCommand;
import pl.sgorski.nethelt.webapi.features.device.dto.request.DeviceCreateRequest;
import pl.sgorski.nethelt.webapi.features.device.dto.response.DeviceResponse;

@Mapper(componentModel = "spring")
public interface DeviceMapper {
  @Mapping(target = "ipAddress", expression = "java(device.getIpAddress().getHostAddress())")
  @Mapping(target = "isEnabled", source = "enabled")
  DeviceResponse toResponse(Device device);

  DeviceCreateCommand toCommand(DeviceCreateRequest request, Long networkId);
}
