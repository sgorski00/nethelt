package pl.sgorski.nethelt.webapi.features.discovery.repository;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityManager;
import java.net.Inet4Address;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.config.PostgresIntegrationTest;
import pl.sgorski.nethelt.webapi.features.device.domain.Ipv4Range;
import pl.sgorski.nethelt.webapi.features.discovery.domain.DiscoveryScan;
import pl.sgorski.nethelt.webapi.features.discovery.domain.DiscoveryScanStatus;
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
    var range = Ipv4Range.parse("192.168.0.0/24");
    var requestedAt =
        discoveryScanRepository.saveAndFlush(new DiscoveryScan(network, range)).getRequestedAt();
    entityManager.clear();

    var scan = discoveryScanRepository.findByNetworkIdForUpdate(network.getId()).orElseThrow();

    assertEquals(range, scan.getRange());
    assertEquals(requestedAt.truncatedTo(ChronoUnit.MICROS), scan.getRequestedAt());
  }

  @Test
  void findByNetworkId_shouldReturnResultsOrderedByIpAddress() {
    var scan = new DiscoveryScan(network, null);
    scan.start(Ipv4Range.parse("192.168.0.0/24"));
    scan.addResult(Inet4Address.ofLiteral("192.168.0.100"), "laptop", 5);
    scan.addResult(Inet4Address.ofLiteral("192.168.0.9"), null, 2);
    scan.addResult(Inet4Address.ofLiteral("192.168.0.20"), "phone", 3);
    scan.complete();
    discoveryScanRepository.saveAndFlush(scan);
    entityManager.clear();

    var result = discoveryScanRepository.findByNetworkId(network.getId()).orElseThrow();

    assertEquals(
        List.of("192.168.0.9", "192.168.0.20", "192.168.0.100"),
        result.getResults().stream().map(r -> r.getIpAddress().getHostAddress()).toList());
  }

  @Test
  void restart_shouldDeletePreviousResults() {
    var scan = new DiscoveryScan(network, null);
    scan.start(Ipv4Range.parse("192.168.0.0/24"));
    scan.addResult(Inet4Address.ofLiteral("192.168.0.1"), "router", 1);
    scan.complete();
    discoveryScanRepository.saveAndFlush(scan);

    scan.restart(null);
    discoveryScanRepository.saveAndFlush(scan);
    entityManager.clear();

    var count =
        entityManager
            .createQuery("SELECT count(r) FROM DiscoveryScanResult r", Long.class)
            .getSingleResult();
    assertEquals(0, count);
  }

  @Test
  void findIdsByStatusInAndRequestedAtBefore_shouldReturnOnlyMatchingScans() {
    var user = TestUserFactory.createLocalUser("discovery-other@example.com");
    var otherNetwork = TestNetworkFactory.createNetwork(user, "Other", null);
    var thirdNetwork = TestNetworkFactory.createNetwork(user, "Third", null);
    entityManager.persist(user);
    entityManager.persist(otherNetwork);
    entityManager.persist(thirdNetwork);
    var oldPending = save(new DiscoveryScan(network, null), Instant.now().minusSeconds(3600));
    var oldCompleted = new DiscoveryScan(otherNetwork, null);
    oldCompleted.start(Ipv4Range.parse("192.168.0.0/24"));
    oldCompleted.complete();
    save(oldCompleted, Instant.now().minusSeconds(3600));
    save(new DiscoveryScan(thirdNetwork, null), Instant.now());

    var ids =
        discoveryScanRepository.findIdsByStatusInAndRequestedAtBefore(
            List.of(DiscoveryScanStatus.PENDING, DiscoveryScanStatus.RUNNING),
            Instant.now().minusSeconds(600));

    assertEquals(List.of(oldPending.getId()), ids);
  }

  @Test
  void findByNetworkIdForUpdate_shouldReturnEmpty_whenNetworkWasNeverScanned() {
    assertTrue(discoveryScanRepository.findByNetworkIdForUpdate(network.getId()).isEmpty());
  }

  @Test
  void saveAndFlush_shouldThrow_whenNetworkAlreadyHasScan() {
    discoveryScanRepository.saveAndFlush(new DiscoveryScan(network, null));

    assertThrows(
        DataIntegrityViolationException.class,
        () -> discoveryScanRepository.saveAndFlush(new DiscoveryScan(network, null)));
  }

  private DiscoveryScan save(DiscoveryScan scan, Instant requestedAt) {
    ReflectionTestUtils.setField(scan, "requestedAt", requestedAt);
    return discoveryScanRepository.saveAndFlush(scan);
  }
}
