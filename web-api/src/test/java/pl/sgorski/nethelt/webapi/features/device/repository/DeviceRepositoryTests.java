package pl.sgorski.nethelt.webapi.features.device.repository;

import static org.junit.jupiter.api.Assertions.*;
import static pl.sgorski.nethelt.webapi.features.device.repository.DeviceSpecifications.*;

import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.config.PostgresIntegrationTest;
import pl.sgorski.nethelt.webapi.features.device.domain.Device;
import pl.sgorski.nethelt.webapi.features.device.domain.DeviceType;
import pl.sgorski.nethelt.webapi.features.device.domain.Ipv4Range;
import pl.sgorski.nethelt.webapi.features.network.domain.Network;
import pl.sgorski.nethelt.webapi.utils.TestDeviceFactory;
import pl.sgorski.nethelt.webapi.utils.TestNetworkFactory;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;

@Transactional
public class DeviceRepositoryTests extends PostgresIntegrationTest {

  @Autowired private DeviceRepository deviceRepository;
  @Autowired private EntityManager entityManager;

  private Network network;

  @BeforeEach
  void setUp() {
    var user = TestUserFactory.createLocalUser("devices@example.com");
    network = TestNetworkFactory.createNetwork(user, "Devices", null);
    var otherNetwork = TestNetworkFactory.createNetwork(user, "Other", null);
    entityManager.persist(user);
    entityManager.persist(network);
    entityManager.persist(otherNetwork);
    persist(network, "Router", "192.168.0.1", DeviceType.NETWORK_DEVICE);
    persist(network, "Laptop", "192.168.0.120", DeviceType.LAN_CLIENT);
    persist(network, "Phone", "192.168.1.15", DeviceType.WIFI_CLIENT);
    persist(network, "Server", "10.0.0.20", DeviceType.LAN_CLIENT);
    persist(network, "Broadcast", "255.255.255.255", DeviceType.NETWORK_DEVICE);
    persist(otherNetwork, "Other router", "192.168.0.1", DeviceType.NETWORK_DEVICE);
    entityManager.flush();
  }

  @Test
  void findAll_shouldReturnDevicesInSubnet() {
    assertEquals(List.of("Router", "Laptop"), findNames(ip("192.168.0.0/24")));
  }

  @Test
  void findAll_shouldReturnDevicesInAnyOfRanges() {
    assertEquals(
        List.of("Server", "Laptop", "Phone", "Broadcast"),
        findNames(ip("10.0.0.0-10.0.0.255, 192.168.0.100-192.168.1.255, 255.255.255.255")));
  }

  @Test
  void findAll_shouldCombineIpNameAndTypeFilters() {
    var specification =
        ip("192.168.0.0/16").and(nameContains("o")).and(hasType(DeviceType.NETWORK_DEVICE));

    assertEquals(List.of("Router"), findNames(specification));
  }

  @Test
  void findAll_shouldMatchNameCaseInsensitivelyAndEscapeWildcards() {
    assertEquals(List.of("Laptop"), findNames(inNetwork(network.getId()).and(nameContains("APT"))));
    assertEquals(List.of(), findNames(inNetwork(network.getId()).and(nameContains("%"))));
  }

  private Specification<Device> ip(String filter) {
    return inNetwork(network.getId()).and(ipInAnyRange(Ipv4Range.parseAll(filter)));
  }

  private List<String> findNames(Specification<Device> specification) {
    return deviceRepository.findAll(specification, Sort.by("ipNumeric")).stream()
        .map(Device::getName)
        .toList();
  }

  private void persist(Network network, String name, String ip, DeviceType type) {
    entityManager.persist(TestDeviceFactory.createDevice(network, name, ip, type));
  }
}
