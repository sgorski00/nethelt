package pl.sgorski.nethelt.webapi.features.discovery.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import pl.sgorski.nethelt.webapi.exception.domain.agent.AgentNotFoundException;
import pl.sgorski.nethelt.webapi.exception.domain.device.InvalidIpRangeException;
import pl.sgorski.nethelt.webapi.exception.domain.discovery.DiscoveryScanNotAllowedException;
import pl.sgorski.nethelt.webapi.exception.domain.discovery.DiscoveryScanNotFoundException;
import pl.sgorski.nethelt.webapi.exception.domain.discovery.DiscoveryScanValidationFailedException;
import pl.sgorski.nethelt.webapi.features.agent.domain.Agent;
import pl.sgorski.nethelt.webapi.features.agent.service.AgentWebService;
import pl.sgorski.nethelt.webapi.features.device.domain.Ipv4Range;
import pl.sgorski.nethelt.webapi.features.discovery.config.DiscoveryScanProperties;
import pl.sgorski.nethelt.webapi.features.discovery.domain.DiscoveryScan;
import pl.sgorski.nethelt.webapi.features.discovery.domain.DiscoveryScanStatus;
import pl.sgorski.nethelt.webapi.features.discovery.dto.command.DiscoveryScanStartCommand;
import pl.sgorski.nethelt.webapi.features.discovery.dto.event.DiscoveryScanStatusChangedEvent;
import pl.sgorski.nethelt.webapi.features.discovery.repository.DiscoveryScanRepository;
import pl.sgorski.nethelt.webapi.utils.TestAgentFactory;

@ExtendWith(MockitoExtension.class)
public class DiscoveryScanWebServiceTests {

  private static final Duration TIMEOUT = Duration.ofMinutes(10);

  @Mock private DiscoveryScanRepository discoveryScanRepository;
  @Mock private AgentWebService agentWebService;
  @Mock private ApplicationEventPublisher eventPublisher;
  private DiscoveryScanWebService discoveryScanWebService;

  private Agent agent;

  @BeforeEach
  void setUp() {
    discoveryScanWebService =
        new DiscoveryScanWebService(
            discoveryScanRepository,
            agentWebService,
            new DiscoveryScanProperties(TIMEOUT),
            eventPublisher);
    agent = TestAgentFactory.createAgentWithId(1L, 1L);
    agent.heartbeat();
  }

  @Test
  void getScan_shouldReturnScan_whenFound() {
    var scan = new DiscoveryScan(agent.getNetwork(), null);
    when(discoveryScanRepository.findByNetworkId(1L)).thenReturn(Optional.of(scan));

    assertSame(scan, discoveryScanWebService.getScan(1L));
  }

  @Test
  void getScan_shouldThrow_whenNotFound() {
    when(discoveryScanRepository.findByNetworkId(1L)).thenReturn(Optional.empty());

    assertThrows(DiscoveryScanNotFoundException.class, () -> discoveryScanWebService.getScan(1L));
  }

  @Test
  void startScan_shouldCreateScan_whenNetworkWasNeverScanned() {
    mockAgent();
    when(discoveryScanRepository.findByNetworkIdForUpdate(1L)).thenReturn(Optional.empty());
    when(discoveryScanRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

    var scan = discoveryScanWebService.startScan(command("192.168.0.0/24"));

    assertSame(agent.getNetwork(), scan.getNetwork());
    assertEquals(DiscoveryScanStatus.PENDING, scan.getStatus());
    assertEquals(Ipv4Range.parse("192.168.0.0/24"), scan.getRange());
    assertEquals(List.of(DiscoveryScanStatus.PENDING), publishedStatuses(1));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "  "})
  void startScan_shouldLeaveRangeEmpty_whenNotSet(String ipRange) {
    mockAgent();
    when(discoveryScanRepository.findByNetworkIdForUpdate(1L)).thenReturn(Optional.empty());
    when(discoveryScanRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

    assertNull(discoveryScanWebService.startScan(command(ipRange)).getRange());
    assertNull(discoveryScanWebService.startScan(command(null)).getRange());
  }

  @Test
  void startScan_shouldThrow_whenFirstScanIsCreatedConcurrently() {
    mockAgent();
    when(discoveryScanRepository.findByNetworkIdForUpdate(1L)).thenReturn(Optional.empty());
    when(discoveryScanRepository.saveAndFlush(any()))
        .thenThrow(new DataIntegrityViolationException("duplicate network_id"));

    assertThrows(
        DiscoveryScanNotAllowedException.class,
        () -> discoveryScanWebService.startScan(command(null)));
    verifyNoInteractions(eventPublisher);
  }

  @Test
  void startScan_shouldRestartPreviousScan_whenItIsFinished() {
    mockAgent();
    var scan = new DiscoveryScan(agent.getNetwork(), null);
    ReflectionTestUtils.setField(scan, "status", DiscoveryScanStatus.COMPLETED);
    when(discoveryScanRepository.findByNetworkIdForUpdate(1L)).thenReturn(Optional.of(scan));

    var result = discoveryScanWebService.startScan(command("10.0.0.1-10.0.0.20"));

    assertSame(scan, result);
    assertEquals(DiscoveryScanStatus.PENDING, scan.getStatus());
    assertEquals(Ipv4Range.parse("10.0.0.1-10.0.0.20"), scan.getRange());
    verify(discoveryScanRepository, never()).saveAndFlush(any());
    assertEquals(List.of(DiscoveryScanStatus.PENDING), publishedStatuses(1));
  }

  @Test
  void startScan_shouldThrow_whenScanIsInProgress() {
    mockAgent();
    var scan = new DiscoveryScan(agent.getNetwork(), null);
    var requestedAt = scan.getRequestedAt();
    when(discoveryScanRepository.findByNetworkIdForUpdate(1L)).thenReturn(Optional.of(scan));

    assertThrows(
        DiscoveryScanNotAllowedException.class,
        () -> discoveryScanWebService.startScan(command(null)));
    assertEquals(requestedAt, scan.getRequestedAt());
    verifyNoInteractions(eventPublisher);
  }

  @Test
  void startScan_shouldRestartScan_whenScanInProgressHasTimedOut() {
    mockAgent();
    var requestedAt = Instant.now().minus(TIMEOUT).minusSeconds(1);
    var scan = new DiscoveryScan(agent.getNetwork(), null);
    ReflectionTestUtils.setField(scan, "requestedAt", requestedAt);
    ReflectionTestUtils.setField(scan, "status", DiscoveryScanStatus.RUNNING);
    when(discoveryScanRepository.findByNetworkIdForUpdate(1L)).thenReturn(Optional.of(scan));

    discoveryScanWebService.startScan(command(null));

    assertEquals(DiscoveryScanStatus.PENDING, scan.getStatus());
    assertTrue(scan.getRequestedAt().isAfter(requestedAt));
    assertEquals(
        List.of(DiscoveryScanStatus.TIMED_OUT, DiscoveryScanStatus.PENDING), publishedStatuses(2));
  }

  @Test
  void startScan_shouldThrow_whenAgentIsOffline() {
    var offlineAgent = TestAgentFactory.createAgentWithId(1L, 1L);
    ReflectionTestUtils.setField(offlineAgent, "lastHeartbeatAt", Instant.now().minusSeconds(3600));
    when(agentWebService.getAgent(1L)).thenReturn(offlineAgent);

    assertThrows(
        DiscoveryScanNotAllowedException.class,
        () -> discoveryScanWebService.startScan(command(null)));
    verifyNoInteractions(discoveryScanRepository);
  }

  @Test
  void startScan_shouldThrow_whenAgentNeverConnected() {
    when(agentWebService.getAgent(1L)).thenReturn(TestAgentFactory.createAgentWithId(1L, 1L));

    assertThrows(
        DiscoveryScanNotAllowedException.class,
        () -> discoveryScanWebService.startScan(command(null)));
    verifyNoInteractions(discoveryScanRepository);
  }

  @Test
  void startScan_shouldThrow_whenAgentIsDisabled() {
    agent.deactivate();
    mockAgent();

    assertThrows(
        DiscoveryScanNotAllowedException.class,
        () -> discoveryScanWebService.startScan(command(null)));
    verifyNoInteractions(discoveryScanRepository);
  }

  @Test
  void startScan_shouldThrow_whenAgentNotFound() {
    when(agentWebService.getAgent(1L)).thenThrow(new AgentNotFoundException());

    assertThrows(
        AgentNotFoundException.class, () -> discoveryScanWebService.startScan(command(null)));
    verifyNoInteractions(discoveryScanRepository);
  }

  @ParameterizedTest
  @ValueSource(strings = {"192.168.0.0/23", "10.0.0.0-10.0.1.0", "0.0.0.0/0"})
  void startScan_shouldThrow_whenRangeIsLargerThan24(String ipRange) {
    mockAgent();
    when(discoveryScanRepository.findByNetworkIdForUpdate(1L)).thenReturn(Optional.empty());

    assertThrows(
        DiscoveryScanValidationFailedException.class,
        () -> discoveryScanWebService.startScan(command(ipRange)));
    verify(discoveryScanRepository, never()).saveAndFlush(any());
  }

  @ParameterizedTest
  @ValueSource(strings = {"192.168.0.0/24", "10.0.0.0-10.0.0.255", "8.8.8.8"})
  void startScan_shouldAcceptRangeUpTo24(String ipRange) {
    mockAgent();
    when(discoveryScanRepository.findByNetworkIdForUpdate(1L)).thenReturn(Optional.empty());
    when(discoveryScanRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

    assertEquals(
        Ipv4Range.parse(ipRange), discoveryScanWebService.startScan(command(ipRange)).getRange());
  }

  @Test
  void startScan_shouldThrow_whenRangeIsInvalid() {
    assertThrows(
        InvalidIpRangeException.class,
        () -> discoveryScanWebService.startScan(command("192.168.0.0/33")));
    verifyNoInteractions(agentWebService, discoveryScanRepository);
  }

  private List<DiscoveryScanStatus> publishedStatuses(int times) {
    var captor = ArgumentCaptor.forClass(DiscoveryScanStatusChangedEvent.class);
    verify(eventPublisher, times(times)).publishEvent(captor.capture());
    return captor.getAllValues().stream().map(DiscoveryScanStatusChangedEvent::status).toList();
  }

  private void mockAgent() {
    when(agentWebService.getAgent(1L)).thenReturn(agent);
  }

  private static DiscoveryScanStartCommand command(String ipRange) {
    return new DiscoveryScanStartCommand(1L, ipRange);
  }
}
