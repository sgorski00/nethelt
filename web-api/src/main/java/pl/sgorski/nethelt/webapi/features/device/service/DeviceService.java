package pl.sgorski.nethelt.webapi.features.device.service;

import java.net.Inet4Address;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.domain.device.DeviceNotFoundException;
import pl.sgorski.nethelt.webapi.exception.domain.device.DeviceValidationFailedException;
import pl.sgorski.nethelt.webapi.features.device.domain.Device;
import pl.sgorski.nethelt.webapi.features.device.dto.command.DeviceCreateCommand;
import pl.sgorski.nethelt.webapi.features.device.dto.command.DeviceUpdateCommand;
import pl.sgorski.nethelt.webapi.features.device.repository.DeviceRepository;
import pl.sgorski.nethelt.webapi.features.network.service.NetworkService;

@Service
@RequiredArgsConstructor
public class DeviceService {

  private final DeviceRepository deviceRepository;
  private final NetworkService networkService;

  public Device getDevice(Long id) {
    return deviceRepository.findById(id).orElseThrow(DeviceNotFoundException::new);
  }

  public Set<Device> getAllDevicesInNetwork(Long networkId) {
    return deviceRepository.findAllByNetworkId(networkId);
  }

  @Transactional
  public Device createDevice(DeviceCreateCommand command) {
    var network = networkService.getNetwork(command.networkId());
    var ipAddress = parse(command.ipAddress());
    var device = new Device(network, command.name(), ipAddress, command.type());
    return deviceRepository.save(device);
  }

  @Transactional
  public Device updateDevice(Long id, DeviceUpdateCommand command) {
    var device = getDevice(id);
    var ipAddress = command.ipAddress() != null ? parse(command.ipAddress()) : null;
    device.update(command.name(), ipAddress, command.type());
    return device;
  }

  @Transactional
  public Device enableDevice(Long id) {
    var device = getDevice(id);
    device.enable();
    return device;
  }

  @Transactional
  public Device disableDevice(Long id) {
    var device = getDevice(id);
    device.disable();
    return device;
  }

  @Transactional
  public void deleteDevice(Long id) {
    var device = getDevice(id);
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
