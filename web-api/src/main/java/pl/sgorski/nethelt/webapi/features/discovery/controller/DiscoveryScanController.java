package pl.sgorski.nethelt.webapi.features.discovery.controller;

import jakarta.validation.Valid;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import pl.sgorski.nethelt.webapi.features.device.service.DeviceService;
import pl.sgorski.nethelt.webapi.features.discovery.dto.request.DiscoveryScanStartRequest;
import pl.sgorski.nethelt.webapi.features.discovery.dto.response.DiscoveryScanResponse;
import pl.sgorski.nethelt.webapi.features.discovery.mapper.DiscoveryScanMapper;
import pl.sgorski.nethelt.webapi.features.discovery.service.DiscoveryScanWebService;

@RestController
@RequestMapping(value = "/networks/{networkId}/discovery-scan", version = "1")
@RequiredArgsConstructor
@PreAuthorize("@networkAuthorization.isOwner(authentication, #networkId)")
public class DiscoveryScanController {

  private final DiscoveryScanWebService discoveryScanWebService;
  private final DiscoveryScanMapper discoveryScanMapper;
  private final DeviceService deviceService;

  @PostMapping
  public ResponseEntity<DiscoveryScanResponse> startScan(
      @P("networkId") @PathVariable("networkId") Long networkId,
      @RequestBody @Valid DiscoveryScanStartRequest request) {
    var command = discoveryScanMapper.toCommand(networkId, request);
    var scan = discoveryScanWebService.startScan(command);
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(discoveryScanMapper.toResponse(scan, Set.of()));
  }

  @GetMapping
  public ResponseEntity<DiscoveryScanResponse> getScan(
      @P("networkId") @PathVariable("networkId") Long networkId) {
    var scan = discoveryScanWebService.getScan(networkId);
    var devices = deviceService.getAllDevicesInNetwork(networkId);
    return ResponseEntity.ok(discoveryScanMapper.toResponse(scan, devices));
  }
}
