package pl.sgorski.nethelt.webapi.features.agent.mapper;

import org.mapstruct.Mapper;
import pl.sgorski.nethelt.webapi.features.agent.domain.Agent;
import pl.sgorski.nethelt.webapi.features.agent.dto.command.AgentCreateCommand;
import pl.sgorski.nethelt.webapi.features.agent.dto.request.AgentCreateRequest;
import pl.sgorski.nethelt.webapi.features.agent.dto.request.AgentUpdateRequest;
import pl.sgorski.nethelt.webapi.features.agent.dto.response.AgentResponse;

@Mapper(componentModel = "spring")
public interface AgentMapper {
  AgentResponse toResponse(Agent agent);

  AgentCreateCommand toCommand(Long networkId, AgentCreateRequest request);

  AgentUpdateCommand toCommand(Long networkId, AgentUpdateRequest request);
}
