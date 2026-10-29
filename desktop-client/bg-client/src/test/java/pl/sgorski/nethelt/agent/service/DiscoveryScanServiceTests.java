package pl.sgorski.nethelt.agent.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.net.Inet4Address;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.agent.exception.NetworkException;
import pl.sgorski.nethelt.agent.exception.WebClientException;
import pl.sgorski.nethelt.agent.mapper.DiscoveryScanMapper;
import pl.sgorski.nethelt.agent.model.discovery.DiscoveredDevice;
import pl.sgorski.nethelt.agent.model.discovery.IpRange;
import pl.sgorski.nethelt.agent.network.discovery.DiscoveryScanner;
import pl.sgorski.nethelt.agent.network.discovery.LocalSubnetResolver;
import pl.sgorski.nethelt.agent.webclient.api.web.DiscoveryScanClient;
import pl.sgorski.nethelt.agent.webclient.dto.request.DiscoveryScanAckRequest;
import pl.sgorski.nethelt.agent.webclient.dto.request.DiscoveryScanFailureRequest;
import pl.sgorski.nethelt.agent.webclient.dto.request.DiscoveryScanResultRequest;
import pl.sgorski.nethelt.agent.webclient.dto.request.DiscoveryScanResultsRequest;
import pl.sgorski.nethelt.agent.webclient.dto.response.DiscoveryScanResponse;

@ExtendWith(MockitoExtension.class)
public class DiscoveryScanServiceTests {

  private static final long WAIT_MS = 2000;
  private static final IpRange RANGE = IpRange.of(ip("192.168.0.1"), ip("192.168.0.254"));

  @Mock private DiscoveryScanClient discoveryScanClient;
  @Mock private LocalSubnetResolver localSubnetResolver;
  @Mock private DiscoveryScanner discoveryScanner;
  private DiscoveryScanService discoveryScanService;

  @BeforeEach
  void setUp() {
    discoveryScanService =
        new DiscoveryScanService(
            discoveryScanClient, new DiscoveryScanMapper(), localSubnetResolver, discoveryScanner);
  }

  @Test
  void startPendingScan_shouldDoNothing_whenNoScanIsPending() {
    when(discoveryScanClient.fetchPendingScan()).thenReturn(Optional.empty());

    discoveryScanService.startPendingScan();

    verify(discoveryScanClient, never()).acknowledgeScan(any());
    verifyNoInteractions(localSubnetResolver, discoveryScanner);
  }

  @Test
  void startPendingScan_shouldScanRangeFromServerAndSendResults() {
    mockPendingScan("192.168.0.1", "192.168.0.254");
    when(discoveryScanner.scan(RANGE))
        .thenReturn(List.of(new DiscoveredDevice(ip("192.168.0.1"), "router.lan", 2)));

    discoveryScanService.startPendingScan();

    verify(discoveryScanClient)
        .acknowledgeScan(new DiscoveryScanAckRequest("192.168.0.1", "192.168.0.254"));
    verify(discoveryScanClient, timeout(WAIT_MS))
        .sendResults(
            new DiscoveryScanResultsRequest(
                List.of(new DiscoveryScanResultRequest("192.168.0.1", "router.lan", 2))));
    verifyNoInteractions(localSubnetResolver);
    verify(discoveryScanClient, never()).reportFailure(any());
  }

  @Test
  void startPendingScan_shouldScanLocalSubnet_whenRangeIsNotSet() {
    mockPendingScan(null, null);
    when(localSubnetResolver.resolve()).thenReturn(RANGE);
    when(discoveryScanner.scan(RANGE)).thenReturn(List.of());

    discoveryScanService.startPendingScan();

    verify(discoveryScanClient)
        .acknowledgeScan(new DiscoveryScanAckRequest("192.168.0.1", "192.168.0.254"));
    verify(discoveryScanClient, timeout(WAIT_MS))
        .sendResults(new DiscoveryScanResultsRequest(List.of()));
  }

  @Test
  void startPendingScan_shouldReportFailure_whenLocalSubnetCannotBeResolved() {
    mockPendingScan(null, null);
    when(localSubnetResolver.resolve())
        .thenThrow(new NetworkException("No IPv4 network interface found"));

    discoveryScanService.startPendingScan();

    verify(discoveryScanClient)
        .reportFailure(new DiscoveryScanFailureRequest("No IPv4 network interface found"));
    verify(discoveryScanClient, never()).acknowledgeScan(any());
    verifyNoInteractions(discoveryScanner);
  }

  @Test
  void startPendingScan_shouldReportFailure_whenRangeFromServerIsInvalid() {
    mockPendingScan("192.168.0.300", "192.168.0.1");

    discoveryScanService.startPendingScan();

    verify(discoveryScanClient).reportFailure(any());
    verify(discoveryScanClient, never()).acknowledgeScan(any());
    verifyNoInteractions(discoveryScanner);
  }

  @Test
  void startPendingScan_shouldReportFailure_whenScanFails() {
    mockPendingScan("192.168.0.1", "192.168.0.254");
    when(discoveryScanner.scan(RANGE))
        .thenThrow(new NetworkException("Discovery scan was interrupted"));

    discoveryScanService.startPendingScan();

    verify(discoveryScanClient, timeout(WAIT_MS))
        .reportFailure(new DiscoveryScanFailureRequest("Discovery scan was interrupted"));
    verify(discoveryScanClient, never()).sendResults(any());
  }

  @Test
  void startPendingScan_shouldReportFailure_whenSendingResultsFails() {
    mockPendingScan("192.168.0.1", "192.168.0.254");
    when(discoveryScanner.scan(RANGE)).thenReturn(List.of());
    doThrow(new WebClientException("Web API request failed with status 422"))
        .when(discoveryScanClient)
        .sendResults(any());

    discoveryScanService.startPendingScan();

    verify(discoveryScanClient, timeout(WAIT_MS))
        .reportFailure(new DiscoveryScanFailureRequest("Web API request failed with status 422"));
  }

  @Test
  void startPendingScan_shouldNotScan_whenAcknowledgeFails() {
    mockPendingScan("192.168.0.1", "192.168.0.254");
    doThrow(new WebClientException("Web API request failed with status 409"))
        .when(discoveryScanClient)
        .acknowledgeScan(any());

    assertThrows(WebClientException.class, () -> discoveryScanService.startPendingScan());
    verifyNoInteractions(discoveryScanner);

    assertThrows(WebClientException.class, () -> discoveryScanService.startPendingScan());
    verify(discoveryScanClient, times(2)).fetchPendingScan();
  }

  @Test
  void startPendingScan_shouldNotPoll_whileScanIsRunning() throws Exception {
    mockPendingScan("192.168.0.1", "192.168.0.254");
    var scanStarted = new CountDownLatch(1);
    var finishScan = new CountDownLatch(1);
    when(discoveryScanner.scan(RANGE))
        .thenAnswer(
            _ -> {
              scanStarted.countDown();
              finishScan.await(WAIT_MS, TimeUnit.MILLISECONDS);
              return List.of();
            });

    discoveryScanService.startPendingScan();
    assertTrue(scanStarted.await(WAIT_MS, TimeUnit.MILLISECONDS));
    discoveryScanService.startPendingScan();

    verify(discoveryScanClient, times(1)).fetchPendingScan();
    finishScan.countDown();
    verify(discoveryScanClient, timeout(WAIT_MS)).sendResults(any());
  }

  private void mockPendingScan(String rangeStart, String rangeEnd) {
    when(discoveryScanClient.fetchPendingScan())
        .thenReturn(Optional.of(new DiscoveryScanResponse(rangeStart, rangeEnd)));
  }

  private static Inet4Address ip(String address) {
    return Inet4Address.ofLiteral(address);
  }
}
