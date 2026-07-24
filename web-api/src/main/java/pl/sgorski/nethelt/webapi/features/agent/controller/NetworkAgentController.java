package pl.sgorski.nethelt.webapi.features.agent.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import pl.sgorski.nethelt.webapi.features.agent.dto.request.AgentCreateRequest;
import pl.sgorski.nethelt.webapi.features.agent.dto.request.AgentStatusUpdateRequest;
import pl.sgorski.nethelt.webapi.features.agent.dto.request.AgentUpdateRequest;
import pl.sgorski.nethelt.webapi.features.agent.dto.response.AgentResponse;
import pl.sgorski.nethelt.webapi.features.agent.dto.response.AgentTokenResponse;
import pl.sgorski.nethelt.webapi.features.agent.mapper.AgentMapper;
import pl.sgorski.nethelt.webapi.features.agent.service.AgentService;

@RestController
@RequestMapping(value = "/networks/{networkId}/agent", version = "1")
@RequiredArgsConstructor
public class NetworkAgentController {

  private final AgentService agentService;
  private final AgentMapper agentMapper;

  @PostMapping
  @PreAuthorize("@networkAuthorization.isOwner(authentication, #networkId)")
  public ResponseEntity<AgentTokenResponse> createAgent(
      @P("networkId") @PathVariable("networkId") Long networkId,
      @RequestBody @Valid AgentCreateRequest request,
      Authentication authentication) {
    var command = agentMapper.toCommand(networkId, request);
    var token = agentService.createAgentAndRetrieveRawToken(command);
    return ResponseEntity.status(201).body(new AgentTokenResponse(token));
  }

  @PostMapping("/token")
  @PreAuthorize("@networkAuthorization.isOwner(authentication, #networkId)")
  public ResponseEntity<AgentTokenResponse> renewToken(
      @P("networkId") @PathVariable("networkId") Long networkId, Authentication authentication) {
    var token = agentService.renewToken(networkId);
    return ResponseEntity.ok(new AgentTokenResponse(token));
  }

  @GetMapping
  @PreAuthorize("@networkAuthorization.isOwner(authentication, #networkId)")
  public ResponseEntity<AgentResponse> getAgent(
      @P("networkId") @PathVariable("networkId") Long networkId, Authentication authentication) {
    var agent = agentService.getAgent(networkId);
    return ResponseEntity.ok(agentMapper.toResponse(agent));
  }

  @PatchMapping
  @PreAuthorize("@networkAuthorization.isOwner(authentication, #networkId)")
  public ResponseEntity<AgentResponse> updateAgent(
      @P("networkId") @PathVariable("networkId") Long networkId,
      @RequestBody @Valid AgentUpdateRequest request,
      Authentication authentication) {
    var command = agentMapper.toCommand(networkId, request);
    var agent = agentService.updateAgent(command);
    return ResponseEntity.ok(agentMapper.toResponse(agent));
  }

  @PatchMapping("/status")
  @PreAuthorize("@networkAuthorization.isOwner(authentication, #networkId)")
  public ResponseEntity<AgentResponse> changeStatus(
      @P("networkId") @PathVariable("networkId") Long networkId,
      @RequestParam("request") @Valid AgentStatusUpdateRequest request,
      Authentication authentication) {
    var agent = agentService.changeStatus(networkId, request.status());
    return ResponseEntity.ok(agentMapper.toResponse(agent));
  }

  @DeleteMapping
  @PreAuthorize("@networkAuthorization.isOwner(authentication, #networkId)")
  public ResponseEntity<Void> deleteAgent(
      @P("networkId") @PathVariable("networkId") Long networkId, Authentication authentication) {
    agentService.deleteAgent(networkId);
    return ResponseEntity.noContent().build();
  }
}
