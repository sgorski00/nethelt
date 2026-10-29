package pl.sgorski.nethelt.webapi.features.discovery.scheduler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.features.discovery.service.DiscoveryScanTimeoutService;

@ExtendWith(MockitoExtension.class)
public class DiscoveryScanTimeoutJobTests {

  @Mock private DiscoveryScanTimeoutService discoveryScanTimeoutService;
  @InjectMocks private DiscoveryScanTimeoutJob discoveryScanTimeoutJob;

  @Test
  void task_shouldTimeOutEachExpiredScan() {
    when(discoveryScanTimeoutService.findExpiredScanIds()).thenReturn(List.of(1L, 2L));

    discoveryScanTimeoutJob.timeOutExpiredScans();

    verify(discoveryScanTimeoutService).timeOut(1L);
    verify(discoveryScanTimeoutService).timeOut(2L);
  }

  @Test
  void task_shouldContinue_whenTimingOutOneScanFails() {
    when(discoveryScanTimeoutService.findExpiredScanIds()).thenReturn(List.of(1L, 2L));
    doThrow(new RuntimeException("DB down")).when(discoveryScanTimeoutService).timeOut(1L);

    assertDoesNotThrow(() -> discoveryScanTimeoutJob.timeOutExpiredScans());
    verify(discoveryScanTimeoutService).timeOut(2L);
  }

  @Test
  void task_shouldNotThrow_whenServiceThrows() {
    when(discoveryScanTimeoutService.findExpiredScanIds())
        .thenThrow(new RuntimeException("DB down"));

    assertDoesNotThrow(() -> discoveryScanTimeoutJob.timeOutExpiredScans());
    verify(discoveryScanTimeoutService, never()).timeOut(any());
  }
}
