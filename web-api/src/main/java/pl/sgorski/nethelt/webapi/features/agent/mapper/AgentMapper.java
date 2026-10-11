package pl.sgorski.nethelt.webapi.features.agent.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.sgorski.nethelt.webapi.features.agent.domain.Agent;
import pl.sgorski.nethelt.webapi.features.agent.domain.AgentConfiguration;
import pl.sgorski.nethelt.webapi.features.agent.dto.command.AgentConfigurationUpdateCommand;
import pl.sgorski.nethelt.webapi.features.agent.dto.command.AgentCreateCommand;
import pl.sgorski.nethelt.webapi.features.agent.dto.request.AgentConfigurationUpdateRequest;
import pl.sgorski.nethelt.webapi.features.agent.dto.request.AgentCreateRequest;
import pl.sgorski.nethelt.webapi.features.agent.dto.request.AgentUpdateRequest;
import pl.sgorski.nethelt.webapi.features.agent.dto.response.AgentConfigurationResponse;
import pl.sgorski.nethelt.webapi.features.agent.dto.response.AgentResponse;

@Mapper(componentModel = "spring")
public interface AgentMapper {
  @Mapping(
      target = "connectionState",
      expression = "java(agent.connectionState(java.time.Instant.now()))")
  AgentResponse toResponse(Agent agent);

  @Mapping(
      target = "maxUnavailabilitySeconds",
      expression = "java(configuration.getMaxUnavailabilityTime().toSeconds())")
  AgentConfigurationResponse toResponse(AgentConfiguration configuration);

  AgentCreateCommand toCommand(Long networkId, AgentCreateRequest request);

  AgentUpdateCommand toCommand(Long networkId, AgentUpdateRequest request);

  AgentConfigurationUpdateCommand toCommand(
      Long networkId, AgentConfigurationUpdateRequest request);
}
