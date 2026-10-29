package pl.sgorski.nethelt.agent.scheduler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.agent.exception.MissingCredentialsException;
import pl.sgorski.nethelt.agent.exception.WebClientException;
import pl.sgorski.nethelt.agent.service.DiscoveryScanService;

@ExtendWith(MockitoExtension.class)
public class DiscoveryScanSchedulerTests {

  @Mock private DiscoveryScanService discoveryScanService;
  @InjectMocks private DiscoveryScanScheduler discoveryScanScheduler;

  @Test
  void checkPendingScan_shouldStartPendingScan() {
    discoveryScanScheduler.checkPendingScan();

    verify(discoveryScanService).startPendingScan();
  }

  @Test
  void checkPendingScan_shouldNotThrow_whenRequestFails() {
    doThrow(new WebClientException("failed")).when(discoveryScanService).startPendingScan();

    assertDoesNotThrow(() -> discoveryScanScheduler.checkPendingScan());
  }

  @Test
  void checkPendingScan_shouldNotThrow_whenCredentialsAreMissing() {
    doThrow(new MissingCredentialsException("no PAT"))
        .when(discoveryScanService)
        .startPendingScan();

    assertDoesNotThrow(() -> discoveryScanScheduler.checkPendingScan());
  }
}
