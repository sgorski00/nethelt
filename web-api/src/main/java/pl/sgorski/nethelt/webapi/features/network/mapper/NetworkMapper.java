package pl.sgorski.nethelt.webapi.features.network.mapper;

import org.mapstruct.Mapper;
import pl.sgorski.nethelt.webapi.features.network.domain.Network;
import pl.sgorski.nethelt.webapi.features.network.dto.command.NetworkCreateCommand;
import pl.sgorski.nethelt.webapi.features.network.dto.command.NetworkUpdateCommand;
import pl.sgorski.nethelt.webapi.features.network.dto.request.NetworkCreateRequest;
import pl.sgorski.nethelt.webapi.features.network.dto.request.NetworkUpdateRequest;
import pl.sgorski.nethelt.webapi.features.network.dto.response.NetworkResponse;

@Mapper(componentModel = "spring")
public interface NetworkMapper {
  NetworkResponse toResponse(Network network);

  NetworkCreateCommand toCommand(NetworkCreateRequest request, Long userId);

  NetworkUpdateCommand toCommand(NetworkUpdateRequest request);
}
