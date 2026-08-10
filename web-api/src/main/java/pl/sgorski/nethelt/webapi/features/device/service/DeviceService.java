package pl.sgorski.nethelt.webapi.features.device.service;

import java.net.Inet4Address;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.domain.device.DeviceNotFoundException;
import pl.sgorski.nethelt.webapi.exception.domain.device.DeviceValidationFailedException;
import pl.sgorski.nethelt.webapi.features.device.domain.Device;
import pl.sgorski.nethelt.webapi.features.device.domain.DeviceType;
import pl.sgorski.nethelt.webapi.features.device.dto.command.DeviceCreateCommand;
import pl.sgorski.nethelt.webapi.features.device.dto.command.DeviceUpdateCommand;
import pl.sgorski.nethelt.webapi.features.device.repository.DeviceRepository;
import pl.sgorski.nethelt.webapi.features.network.service.NetworkService;

@Service
@RequiredArgsConstructor
public class DeviceService {

  private final DeviceRepository deviceRepository;
  private final NetworkService networkService;

  public Device getDevice(Long networkId, Long deviceId) {
    return deviceRepository
        .findByIdAndNetworkId(deviceId, networkId)
        .orElseThrow(DeviceNotFoundException::new);
  }

  public Set<Device> getAllDevicesInNetwork(Long networkId) {
    return deviceRepository.findAllByNetworkId(networkId);
  }

  public Page<Device> getAllDevicesInNetwork(
      Long networkId, @Nullable DeviceType type, Pageable pageable) {
    return deviceRepository.findAllByNetworkIdAndType(networkId, type, pageable);
  }

  @Transactional
  public Device createDevice(DeviceCreateCommand command) {
    var network = networkService.getNetwork(command.networkId());
    var ipAddress = parse(command.ipAddress());
    var device = new Device(network, command.name(), ipAddress, command.type());
    return deviceRepository.save(device);
  }

  @Transactional
  public Device updateDevice(Long networkId, Long deviceId, DeviceUpdateCommand command) {
    var device = getDevice(networkId, deviceId);
    var ipAddress = command.ipAddress() != null ? parse(command.ipAddress()) : null;
    device.update(command.name(), ipAddress, command.type());
    return device;
  }

  @Transactional
  public Device enableDevice(Long networkId, Long deviceId) {
    var device = getDevice(networkId, deviceId);
    device.enable();
    return device;
  }

  @Transactional
  public Device disableDevice(Long networkId, Long deviceId) {
    var device = getDevice(networkId, deviceId);
    device.disable();
    return device;
  }

  @Transactional
  public void deleteDevice(Long networkId, Long deviceId) {
    var device = getDevice(networkId, deviceId);
    deviceRepository.delete(device);
  }

  private static Inet4Address parse(String ipAddress) {
    try {
      return Inet4Address.ofLiteral(ipAddress);
    } catch (IllegalArgumentException e) {
      throw new DeviceValidationFailedException("invalid IP address: " + ipAddress);
    }
  }
}
