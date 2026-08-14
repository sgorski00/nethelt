package pl.sgorski.nethelt.webapi.features.device.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@PreAuthorize("@networkAuthorization.isOwner(authentication, #networkId)")
public class DeviceController {

  private final DeviceService deviceService;
  private final DeviceMapper deviceMapper;

  @GetMapping
  public ResponseEntity<Page<DeviceResponse>> getAllDevices(
      @P("networkId") @PathVariable("networkId") Long networkId,
      @RequestParam(name = "type", required = false) @Nullable DeviceType type,
      Pageable pageable) {
    var devices =
        deviceService
            .getAllDevicesInNetwork(networkId, type, pageable)
            .map(deviceMapper::toResponse);
    return ResponseEntity.ok(devices);
  }

  @GetMapping("/all")
  public ResponseEntity<List<DeviceResponse>> getAllDevices(
      @P("networkId") @PathVariable("networkId") Long networkId) {
    var devices =
        deviceService.getAllDevicesInNetwork(networkId).stream()
            .map(deviceMapper::toResponse)
            .toList();
    return ResponseEntity.ok(devices);
  }

  @PostMapping
  public ResponseEntity<DeviceResponse> createDevice(
      @P("networkId") @PathVariable("networkId") Long networkId,
      @Valid @RequestBody DeviceCreateRequest request) {
    var command = deviceMapper.toCommand(request, networkId);
    var device = deviceService.createDevice(command);
    return ResponseEntity.status(HttpStatus.CREATED).body(deviceMapper.toResponse(device));
  }

  @PutMapping("/{deviceId}")
  public ResponseEntity<DeviceResponse> updateDevice(
      @P("networkId") @PathVariable("networkId") Long networkId,
      @PathVariable("deviceId") Long deviceId,
      @Valid @RequestBody DeviceUpdateRequest request) {
    var command = deviceMapper.toCommand(request);
    var device = deviceService.updateDevice(networkId, deviceId, command);
    return ResponseEntity.ok(deviceMapper.toResponse(device));
  }

  @PatchMapping("/{deviceId}/enable")
  public ResponseEntity<DeviceResponse> enableDevice(
      @P("networkId") @PathVariable("networkId") Long networkId,
      @PathVariable("deviceId") Long deviceId) {
    var device = deviceService.enableDevice(networkId, deviceId);
    return ResponseEntity.ok(deviceMapper.toResponse(device));
  }

  @PatchMapping("/{deviceId}/disable")
  public ResponseEntity<DeviceResponse> disableDevice(
      @P("networkId") @PathVariable("networkId") Long networkId,
      @PathVariable("deviceId") Long deviceId) {
    var device = deviceService.disableDevice(networkId, deviceId);
    return ResponseEntity.ok(deviceMapper.toResponse(device));
  }

  @DeleteMapping("/{deviceId}")
  public ResponseEntity<Void> deleteDevice(
      @P("networkId") @PathVariable("networkId") Long networkId,
      @PathVariable("deviceId") Long deviceId) {
    deviceService.deleteDevice(networkId, deviceId);
    return ResponseEntity.noContent().build();
  }
}
