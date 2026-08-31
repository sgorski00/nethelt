package pl.sgorski.nethelt.agent.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.webclient.api.web.AgentClient;

@Slf4j
@Component
@RequiredArgsConstructor
public final class HeartbeatScheduler {

  private final AgentClient agentClient;

  @Scheduled(
      fixedDelayString = "${scheduler.heartbeat-interval-seconds}",
      timeUnit = java.util.concurrent.TimeUnit.SECONDS)
  void sendHeartbeat() {
    try {
      agentClient.heartbeat();
    } catch (Exception e) {
      log.error("Error while sending heartbeat to the server: {}", e.getMessage(), e);
    }
  }
}
