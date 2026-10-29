package pl.sgorski.nethelt.webapi.features.discovery.mapper;

import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.sgorski.nethelt.webapi.features.device.domain.Ipv4Range;
import pl.sgorski.nethelt.webapi.features.discovery.domain.DiscoveryScan;
import pl.sgorski.nethelt.webapi.features.discovery.dto.command.DiscoveryScanStartCommand;
import pl.sgorski.nethelt.webapi.features.discovery.dto.request.DiscoveryScanStartRequest;
import pl.sgorski.nethelt.webapi.features.discovery.dto.response.DiscoveryScanResponse;

@Mapper(componentModel = "spring")
public interface DiscoveryScanMapper {
  DiscoveryScanStartCommand toCommand(Long networkId, DiscoveryScanStartRequest request);

  @Mapping(target = "rangeStart", qualifiedByName = "ipLiteral")
  @Mapping(target = "rangeEnd", qualifiedByName = "ipLiteral")
  DiscoveryScanResponse toResponse(DiscoveryScan scan);

  @Named("ipLiteral")
  default @Nullable String toIpLiteral(@Nullable Long address) {
    return address == null ? null : Ipv4Range.toLiteral(address);
  }
}
