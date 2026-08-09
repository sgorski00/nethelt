package pl.sgorski.nethelt.webapi.features.device.controller;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import pl.sgorski.nethelt.webapi.features.device.domain.DeviceType;
import pl.sgorski.nethelt.webapi.features.device.dto.request.DeviceCreateRequest;
import pl.sgorski.nethelt.webapi.features.device.dto.request.DeviceUpdateRequest;
import pl.sgorski.nethelt.webapi.features.device.dto.response.DeviceResponse;
import pl.sgorski.nethelt.webapi.features.device.mapper.DeviceMapper;
import pl.sgorski.nethelt.webapi.features.device.service.DeviceService;

@RestController
@RequestMapping(value = "/networks/{networkId}/devices", version = "1")
@RequiredArgsConstructor
public class DeviceController {

  private final DeviceService deviceService;
  private final DeviceMapper deviceMapper;

  @GetMapping
  @PreAuthorize("@networkAuthorization.isOwner(authentication, #networkId)")
  public ResponseEntity<Page<DeviceResponse>> getAllDevices(
      @P("networkId") @PathVariable("networkId") Long networkId,
      @RequestParam(name = "type", required = false) @Nullable DeviceType type,
      Pageable pageable,
      Authentication authentication) {
    var devices =
        deviceService
            .getAllDevicesInNetwork(networkId, type, pageable)
            .map(deviceMapper::toResponse);
    return ResponseEntity.ok(devices);
  }

  @PostMapping
  public ResponseEntity<DeviceResponse> createDevice(
      @PathVariable("networkId") Long networkId, @Valid @RequestBody DeviceCreateRequest request) {
    var command = deviceMapper.toCommand(request, networkId);
    var device = deviceService.createDevice(command);
    return ResponseEntity.status(HttpStatus.CREATED).body(deviceMapper.toResponse(device));
  }

  @PutMapping("/{deviceId}")
  @PreAuthorize("@networkAuthorization.isOwner(authentication, #networkId)")
  public ResponseEntity<DeviceResponse> updateDevice(
      @P("networkId") @PathVariable("networkId") Long networkId,
      @PathVariable("deviceId") Long id,
      @Valid @RequestBody DeviceUpdateRequest request,
      Authentication authentication) {
    var command = deviceMapper.toCommand(request);
    var device = deviceService.updateDevice(id, command);
    return ResponseEntity.ok(deviceMapper.toResponse(device));
  }

  @PatchMapping("/{deviceId}/enable")
  @PreAuthorize("@networkAuthorization.isOwner(authentication, #networkId)")
  public ResponseEntity<DeviceResponse> enableDevice(
      @P("networkId") @PathVariable("networkId") Long networkId,
      @PathVariable("deviceId") Long id,
      Authentication authentication) {
    var device = deviceService.enableDevice(id);
    return ResponseEntity.ok(deviceMapper.toResponse(device));
  }

  @PatchMapping("/{deviceId}/disable")
  @PreAuthorize("@networkAuthorization.isOwner(authentication, #networkId)")
  public ResponseEntity<DeviceResponse> disableDevice(
      @P("networkId") @PathVariable("networkId") Long networkId,
      @PathVariable("deviceId") Long id,
      Authentication authentication) {
    var device = deviceService.disableDevice(id);
    return ResponseEntity.ok(deviceMapper.toResponse(device));
  }

  @DeleteMapping("/{deviceId}")
  @PreAuthorize("@networkAuthorization.isOwner(authentication, #networkId)")
  public ResponseEntity<Void> deleteDevice(
      @P("networkId") @PathVariable("networkId") Long networkId,
      @PathVariable("deviceId") Long id,
      Authentication authentication) {
    deviceService.deleteDevice(id);
    return ResponseEntity.noContent().build();
  }
}
