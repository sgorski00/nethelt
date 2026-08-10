package pl.sgorski.nethelt.webapi.features.device.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pl.sgorski.nethelt.webapi.exception.domain.device.DeviceNotFoundException;
import pl.sgorski.nethelt.webapi.exception.domain.device.DeviceValidationFailedException;
import pl.sgorski.nethelt.webapi.exception.domain.network.NetworkNotFoundException;
import pl.sgorski.nethelt.webapi.features.device.domain.DeviceType;
import pl.sgorski.nethelt.webapi.features.device.dto.command.DeviceCreateCommand;
import pl.sgorski.nethelt.webapi.features.device.dto.command.DeviceUpdateCommand;
import pl.sgorski.nethelt.webapi.features.device.repository.DeviceRepository;
import pl.sgorski.nethelt.webapi.features.network.service.NetworkService;
import pl.sgorski.nethelt.webapi.utils.TestDeviceFactory;
import pl.sgorski.nethelt.webapi.utils.TestNetworkFactory;

@ExtendWith(MockitoExtension.class)
public class DeviceServiceTests {

  @Mock private DeviceRepository deviceRepository;
  @Mock private NetworkService networkService;
  @InjectMocks private DeviceService deviceService;

  @Test
  void getDevice_shouldReturnDevice_whenDeviceExists() {
    var device = TestDeviceFactory.createDevice();
    when(deviceRepository.findByIdAndNetworkId(1L, 999L)).thenReturn(Optional.of(device));

    var result = deviceService.getDevice(999L, 1L);

    assertSame(device, result);
  }

  @Test
  void getDevice_shouldThrowException_whenDeviceDoesNotExist() {
    when(deviceRepository.findByIdAndNetworkId(1L, 1L)).thenReturn(Optional.empty());

    assertThrows(DeviceNotFoundException.class, () -> deviceService.getDevice(1L, 1L));
  }

  @Test
  void getAllDevicesInNetwork_shouldReturnDevices_Set() {
    var devices =
        Set.of(
            TestDeviceFactory.createDevice("test-device-1", "192.168.1.1"),
            TestDeviceFactory.createDevice("test-device-2", "192.168.1.2"));
    when(deviceRepository.findAllByNetworkId(1L)).thenReturn(devices);

    var result = deviceService.getAllDevicesInNetwork(1L);

    assertIterableEquals(devices, result);
  }

  @Test
  void getAllDevicesInNetwork_shouldReturnDevices_Page() {
    var devices =
        List.of(
            TestDeviceFactory.createDevice("test-device-1", "192.168.1.1"),
            TestDeviceFactory.createDevice("test-device-2", "192.168.1.2"));
    var pageable = PageRequest.of(0, 10);
    when(deviceRepository.findAllByNetworkIdAndType(
            eq(1L), nullable(DeviceType.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(devices));

    var result = deviceService.getAllDevicesInNetwork(1L, null, pageable);

    assertEquals(devices.size(), result.getTotalElements());
    assertIterableEquals(devices, result.getContent());
  }

  @Test
  void createDevice_shouldCreateDevice_whenValidCommand() {
    var network = TestNetworkFactory.createNetwork();
    var command =
        new DeviceCreateCommand(1L, "test-device", "192.168.1.1", DeviceType.NETWORK_DEVICE);
    when(networkService.getNetwork(1L)).thenReturn(network);
    when(deviceRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

    var result = deviceService.createDevice(command);

    assertEquals("test-device", result.getName());
    assertEquals("192.168.1.1", result.getIpAddress().getHostAddress());
    assertEquals(DeviceType.NETWORK_DEVICE, result.getType());
    verify(deviceRepository).save(any());
  }

  @Test
  void createDevice_shouldThrow_whenNetworkNotFound() {
    var command =
        new DeviceCreateCommand(1L, "test-device", "192.168.1.1", DeviceType.NETWORK_DEVICE);
    when(networkService.getNetwork(1L)).thenThrow(new NetworkNotFoundException());

    assertThrows(NetworkNotFoundException.class, () -> deviceService.createDevice(command));
  }

  @Test
  void createDevice_shouldThrow_whenIpNotValid() {
    var network = TestNetworkFactory.createNetwork();
    var command =
        new DeviceCreateCommand(1L, "test-device", "not.val.ida.ddr", DeviceType.NETWORK_DEVICE);
    when(networkService.getNetwork(1L)).thenReturn(network);

    assertThrows(DeviceValidationFailedException.class, () -> deviceService.createDevice(command));
  }

  @Test
  void updateDevice_shouldUpdateDevice_whenValidCommand() {
    var command = new DeviceUpdateCommand("updated-device", "10.0.255.254", DeviceType.WIFI_CLIENT);
    var device = TestDeviceFactory.createDevice();
    when(deviceRepository.findByIdAndNetworkId(1L, 1L)).thenReturn(Optional.of(device));

    var result = deviceService.updateDevice(1L, 1L, command);

    assertSame(device, result);
    assertEquals("updated-device", result.getName());
    assertEquals("10.0.255.254", result.getIpAddress().getHostAddress());
    assertEquals(DeviceType.WIFI_CLIENT, result.getType());
  }

  @Test
  void updateDevice_shouldUpdateDevice_whenValidCommandWithNullIp() {
    var command = new DeviceUpdateCommand("updated-device", null, null);
    var device = TestDeviceFactory.createDevice();
    var oldIpAddress = device.getIpAddress();
    var oldType = device.getType();
    when(deviceRepository.findByIdAndNetworkId(1L, 1L)).thenReturn(Optional.of(device));

    var result = deviceService.updateDevice(1L, 1L, command);

    assertEquals("updated-device", result.getName());
    assertEquals(oldIpAddress, result.getIpAddress());
    assertEquals(oldType, result.getType());
  }

  @Test
  void updateDevice_shouldThrow_whenIpNotValid() {
    var command = new DeviceUpdateCommand(null, "256.256.256.256", null);
    var device = TestDeviceFactory.createDevice();
    when(deviceRepository.findByIdAndNetworkId(1L, 1L)).thenReturn(Optional.of(device));

    assertThrows(
        DeviceValidationFailedException.class, () -> deviceService.updateDevice(1L, 1L, command));
  }

  @Test
  void updateDevice_shouldThrow_whenDeviceNotFound() {
    var command = new DeviceUpdateCommand(null, "256.256.256.256", null);
    when(deviceRepository.findByIdAndNetworkId(1L, 1L)).thenReturn(Optional.empty());

    assertThrows(DeviceNotFoundException.class, () -> deviceService.updateDevice(1L, 1L, command));
  }

  @Test
  void enableDevice_shouldEnableDevice() {
    var device = TestDeviceFactory.createDevice();
    device.disable();
    when(deviceRepository.findByIdAndNetworkId(1L, 1L)).thenReturn(Optional.of(device));

    assertFalse(device.isEnabled());
    var result = deviceService.enableDevice(1L, 1L);

    assertSame(device, result);
    assertTrue(device.isEnabled());
  }

  @Test
  void enableDevice_shouldThrow_whenDeviceNotFound() {
    when(deviceRepository.findByIdAndNetworkId(1L, 1L)).thenReturn(Optional.empty());

    assertThrows(DeviceNotFoundException.class, () -> deviceService.enableDevice(1L, 1L));
  }

  @Test
  void disableDevice_shouldDisableDevice() {
    var device = TestDeviceFactory.createDevice();
    when(deviceRepository.findByIdAndNetworkId(1L, 1L)).thenReturn(Optional.of(device));

    assertTrue(device.isEnabled());
    var result = deviceService.disableDevice(1L, 1L);

    assertSame(device, result);
    assertFalse(device.isEnabled());
  }

  @Test
  void disableDevice_shouldThrow_whenDeviceNotFound() {
    when(deviceRepository.findByIdAndNetworkId(1L, 1L)).thenReturn(Optional.empty());

    assertThrows(DeviceNotFoundException.class, () -> deviceService.disableDevice(1L, 1L));
  }

  @Test
  void delete_shouldDeleteDevice() {
    var device = TestDeviceFactory.createDevice();
    when(deviceRepository.findByIdAndNetworkId(1L, 1L)).thenReturn(Optional.of(device));

    deviceService.deleteDevice(1L, 1L);

    verify(deviceRepository).delete(device);
  }

  @Test
  void delete_shouldThrow_whenDeviceNotFound() {
    when(deviceRepository.findByIdAndNetworkId(1L, 1L)).thenReturn(Optional.empty());

    assertThrows(DeviceNotFoundException.class, () -> deviceService.deleteDevice(1L, 1L));
    verify(deviceRepository, never()).delete(any());
  }
}
