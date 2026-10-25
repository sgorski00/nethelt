package pl.sgorski.nethelt.webapi.features.agent.scheduler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sgorski.nethelt.webapi.features.agent.service.AgentAvailabilityService;

@ExtendWith(MockitoExtension.class)
public class AgentAvailabilityCheckJobTests {

  @Mock private AgentAvailabilityService agentAvailabilityService;
  @InjectMocks private AgentAvailabilityCheckJob agentAvailabilityCheckJob;

  @Test
  void task_shouldMarkEachFoundAgentAsOffline() {
    when(agentAvailabilityService.findAgentIdsToMarkOffline(any())).thenReturn(List.of(1L, 2L));

    agentAvailabilityCheckJob.checkAvailability();

    verify(agentAvailabilityService).markOffline(eq(1L), any());
    verify(agentAvailabilityService).markOffline(eq(2L), any());
  }

  @Test
  void task_shouldContinue_whenMarkingOneAgentFails() {
    when(agentAvailabilityService.findAgentIdsToMarkOffline(any())).thenReturn(List.of(1L, 2L));
    doThrow(new RuntimeException("DB down"))
        .when(agentAvailabilityService)
        .markOffline(eq(1L), any());

    assertDoesNotThrow(() -> agentAvailabilityCheckJob.checkAvailability());
    verify(agentAvailabilityService).markOffline(eq(2L), any());
  }

  @Test
  void task_shouldNotThrow_whenServiceThrows() {
    when(agentAvailabilityService.findAgentIdsToMarkOffline(any()))
        .thenThrow(new RuntimeException("DB down"));

    assertDoesNotThrow(() -> agentAvailabilityCheckJob.checkAvailability());
    verify(agentAvailabilityService, never()).markOffline(any(), any());
  }
}
