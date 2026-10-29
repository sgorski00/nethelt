package pl.sgorski.nethelt.webapi.features.discovery.repository;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.config.PostgresIntegrationTest;
import pl.sgorski.nethelt.webapi.features.device.domain.Ipv4Range;
import pl.sgorski.nethelt.webapi.features.discovery.domain.DiscoveryScan;
import pl.sgorski.nethelt.webapi.features.network.domain.Network;
import pl.sgorski.nethelt.webapi.utils.TestNetworkFactory;
import pl.sgorski.nethelt.webapi.utils.TestUserFactory;

@Transactional
public class DiscoveryScanRepositoryTests extends PostgresIntegrationTest {

  @Autowired private DiscoveryScanRepository discoveryScanRepository;
  @Autowired private EntityManager entityManager;

  private Network network;

  @BeforeEach
  void setUp() {
    var user = TestUserFactory.createLocalUser("discovery@example.com");
    network = TestNetworkFactory.createNetwork(user, "Discovery", null);
    entityManager.persist(user);
    entityManager.persist(network);
  }

  @Test
  void findByNetworkIdForUpdate_shouldReturnScanWithRange() {
    var requestedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
    var range = Ipv4Range.parse("192.168.0.0/24");
    discoveryScanRepository.saveAndFlush(new DiscoveryScan(network, range, requestedAt));
    entityManager.clear();

    var scan = discoveryScanRepository.findByNetworkIdForUpdate(network.getId()).orElseThrow();

    assertEquals(range, scan.getRange());
    assertEquals(requestedAt, scan.getRequestedAt());
  }

  @Test
  void findByNetworkIdForUpdate_shouldReturnEmpty_whenNetworkWasNeverScanned() {
    assertTrue(discoveryScanRepository.findByNetworkIdForUpdate(network.getId()).isEmpty());
  }

  @Test
  void saveAndFlush_shouldThrow_whenNetworkAlreadyHasScan() {
    discoveryScanRepository.saveAndFlush(new DiscoveryScan(network, null, Instant.now()));

    assertThrows(
        DataIntegrityViolationException.class,
        () ->
            discoveryScanRepository.saveAndFlush(new DiscoveryScan(network, null, Instant.now())));
  }
}
