package pl.sgorski.nethelt.webapi.features.agent.scheduler;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.webapi.features.agent.service.AgentAvailabilityService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgentAvailabilityCheckJob {

  private final AgentAvailabilityService agentAvailabilityService;

  @Scheduled(fixedDelayString = "${agent.availability.check-interval}")
  public void checkAvailability() {
    try {
      var now = Instant.now();
      var agentIds = agentAvailabilityService.findAgentIdsToMarkOffline(now);
      log.debug("Marking {} agents as offline", agentIds.size());
      agentIds.forEach(agentId -> markOffline(agentId, now));
    } catch (Exception e) {
      log.error("Error during agent availability check", e);
    }
  }

  private void markOffline(Long agentId, Instant now) {
    try {
      agentAvailabilityService.markOffline(agentId, now);
    } catch (Exception e) {
      log.error("Failed to mark agent with ID {} as offline", agentId, e);
    }
  }
}
