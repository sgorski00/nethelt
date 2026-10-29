package pl.sgorski.nethelt.webapi.features.discovery.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.sgorski.nethelt.webapi.features.discovery.dto.request.DiscoveryScanAckRequest;
import pl.sgorski.nethelt.webapi.features.discovery.dto.request.DiscoveryScanFailureRequest;
import pl.sgorski.nethelt.webapi.features.discovery.dto.request.DiscoveryScanResultsRequest;
import pl.sgorski.nethelt.webapi.features.discovery.dto.response.DiscoveryScanClientResponse;
import pl.sgorski.nethelt.webapi.features.discovery.mapper.DiscoveryScanMapper;
import pl.sgorski.nethelt.webapi.features.discovery.service.DiscoveryScanDesktopService;
import pl.sgorski.nethelt.webapi.security.agent.AgentAuthentication;

@RestController
@RequestMapping(value = "/client/discovery-scan", version = "1")
@RequiredArgsConstructor
public class DiscoveryScanDesktopController {

  private final DiscoveryScanDesktopService discoveryScanDesktopService;
  private final DiscoveryScanMapper discoveryScanMapper;

  @GetMapping("/pending")
  public ResponseEntity<DiscoveryScanClientResponse> getPendingScan(
      AgentAuthentication authentication) {
    return discoveryScanDesktopService
        .getPendingScan(authentication.getPrincipal().networkId())
        .map(discoveryScanMapper::toClientResponse)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.noContent().build());
  }

  @PostMapping("/ack")
  public ResponseEntity<Void> acknowledgeScan(
      AgentAuthentication authentication, @RequestBody @Valid DiscoveryScanAckRequest request) {
    var command = discoveryScanMapper.toCommand(authentication.getPrincipal().networkId(), request);
    discoveryScanDesktopService.acknowledgeScan(command);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/results")
  public ResponseEntity<Void> completeScan(
      AgentAuthentication authentication, @RequestBody @Valid DiscoveryScanResultsRequest request) {
    var command = discoveryScanMapper.toCommand(authentication.getPrincipal().networkId(), request);
    discoveryScanDesktopService.completeScan(command);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/failure")
  public ResponseEntity<Void> failScan(
      AgentAuthentication authentication, @RequestBody @Valid DiscoveryScanFailureRequest request) {
    discoveryScanDesktopService.failScan(
        authentication.getPrincipal().networkId(), request.reason());
    return ResponseEntity.noContent().build();
  }
}
