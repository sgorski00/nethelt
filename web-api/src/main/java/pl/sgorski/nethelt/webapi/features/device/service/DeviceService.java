package pl.sgorski.nethelt.webapi.features.device.service;

import static pl.sgorski.nethelt.webapi.features.device.repository.DeviceSpecifications.*;

import java.net.Inet4Address;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.domain.device.DeviceNotFoundException;
import pl.sgorski.nethelt.webapi.exception.domain.device.DeviceValidationFailedException;
import pl.sgorski.nethelt.webapi.features.device.domain.Device;
import pl.sgorski.nethelt.webapi.features.device.domain.Ipv4Range;
import pl.sgorski.nethelt.webapi.features.device.dto.command.DeviceCreateCommand;
import pl.sgorski.nethelt.webapi.features.device.dto.command.DeviceFilterCommand;
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
      Long networkId, DeviceFilterCommand filter, Pageable pageable) {
    var specification = inNetwork(networkId);
    if (filter.type() != null) {
      specification = specification.and(hasType(filter.type()));
    }
    if (filter.name() != null && !filter.name().isBlank()) {
      specification = specification.and(nameContains(filter.name()));
    }
    if (filter.ip() != null && !filter.ip().isBlank()) {
      specification = specification.and(ipInAnyRange(Ipv4Range.parseAll(filter.ip())));
    }
    return deviceRepository.findAll(specification, pageable);
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
