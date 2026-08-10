package pl.sgorski.nethelt.webapi.features.agent.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import pl.sgorski.nethelt.webapi.features.agent.dto.request.AgentCreateRequest;
import pl.sgorski.nethelt.webapi.features.agent.dto.request.AgentStatusUpdateRequest;
import pl.sgorski.nethelt.webapi.features.agent.dto.request.AgentUpdateRequest;
import pl.sgorski.nethelt.webapi.features.agent.dto.response.AgentResponse;
import pl.sgorski.nethelt.webapi.features.agent.dto.response.AgentTokenResponse;
import pl.sgorski.nethelt.webapi.features.agent.mapper.AgentMapper;
import pl.sgorski.nethelt.webapi.features.agent.service.AgentWebService;

@RestController
@RequestMapping(value = "/networks/{networkId}/agent", version = "1")
@RequiredArgsConstructor
@PreAuthorize("@networkAuthorization.isOwner(authentication, #networkId)")
public class AgentWebController {

  private final AgentWebService agentWebService;
  private final AgentMapper agentMapper;

  @PostMapping
  public ResponseEntity<AgentTokenResponse> createAgent(
      @P("networkId") @PathVariable("networkId") Long networkId,
      @RequestBody @Valid AgentCreateRequest request) {
    var command = agentMapper.toCommand(networkId, request);
    var token = agentWebService.createAgentAndRetrieveRawToken(command);
    return ResponseEntity.status(201).body(new AgentTokenResponse(token));
  }

  @PostMapping("/token")
  public ResponseEntity<AgentTokenResponse> renewToken(
      @P("networkId") @PathVariable("networkId") Long networkId) {
    var token = agentWebService.renewToken(networkId);
    return ResponseEntity.ok(new AgentTokenResponse(token));
  }

  @GetMapping
  public ResponseEntity<AgentResponse> getAgent(
      @P("networkId") @PathVariable("networkId") Long networkId) {
    var agent = agentWebService.getAgent(networkId);
    return ResponseEntity.ok(agentMapper.toResponse(agent));
  }

  @PatchMapping
  public ResponseEntity<AgentResponse> updateAgent(
      @P("networkId") @PathVariable("networkId") Long networkId,
      @RequestBody @Valid AgentUpdateRequest request) {
    var command = agentMapper.toCommand(networkId, request);
    var agent = agentWebService.updateAgent(command);
    return ResponseEntity.ok(agentMapper.toResponse(agent));
  }

  @PatchMapping("/status")
  public ResponseEntity<AgentResponse> changeStatus(
      @P("networkId") @PathVariable("networkId") Long networkId,
      @RequestBody @Valid AgentStatusUpdateRequest request) {
    var agent = agentWebService.changeStatus(networkId, request.status());
    return ResponseEntity.ok(agentMapper.toResponse(agent));
  }

  @DeleteMapping
  public ResponseEntity<Void> deleteAgent(
      @P("networkId") @PathVariable("networkId") Long networkId) {
    agentWebService.deleteAgent(networkId);
    return ResponseEntity.noContent().build();
  }
}
