package pl.sgorski.nethelt.agent.scheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.executor.MonitoringExecutor;
import pl.sgorski.nethelt.agent.webclient.WebClientService;
import pl.sgorski.nethelt.model.Device;
import pl.sgorski.nethelt.model.NetworkConfig;
import pl.sgorski.nethelt.model.PingResult;
import pl.sgorski.nethelt.model.TelnetResult;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebScheduledTaskManager {

  private static final int CONFIG_UPDATE_INTERVAL_SEC = 60;

  private final WebClientService webClientService;
  private final MonitoringExecutor monitoringExecutor;
  private final TaskScheduler scheduler;

  private ScheduledFuture<?> pingTask;
  private int pingIntervalSeconds = -1;
  private boolean pingEnabled = false;

  private ScheduledFuture<?> telnetTask;
  private int telnetIntervalSeconds = -1;
  private boolean telnetEnabled = false;

  @Scheduled(fixedDelay = CONFIG_UPDATE_INTERVAL_SEC, timeUnit = TimeUnit.SECONDS)
  void updateTasks() {
    try {
      var configs = webClientService.fetchNetworkConfig();
      var devices = webClientService.fetchDevices();

      for (var cfg : configs) {
        switch (cfg.getOperation()) {
          case PING -> handlePing(cfg, devices);
          case TELNET -> handleTelnet(cfg, devices);
          default -> log.warn("Unsupported operation: {}", cfg.getOperation());
        }
      }
    } catch (Exception e) {
      log.error("Error while updating tasks: {}", e.getMessage(), e);
    }
  }

  private void handlePing(NetworkConfig cfg, Set<Device> devices) {
    if (!cfg.isChanged(pingEnabled, pingIntervalSeconds)) return;

    log.info("Configuration change detected for PING operation.");
    pingIntervalSeconds = cfg.getIntervalSeconds();
    pingEnabled = cfg.isEnabled();

    closeTask(pingTask);
    if (!pingEnabled) {
      pingTask = null;
      log.info("PING task disabled.");
      return;
    }

    log.info("Rescheduling PING task (enabled).");
    pingTask =
        scheduleTask(
            pingIntervalSeconds,
            () -> {
              var results = monitoringExecutor.getPingResults(devices);
              webClientService.sendResult(results, PingResult.class);
            });
  }

  private void handleTelnet(NetworkConfig cfg, Set<Device> devices) {
    if (!cfg.isChanged(telnetEnabled, telnetIntervalSeconds)) return;

    log.info("Configuration change detected for TELNET operation.");
    telnetIntervalSeconds = cfg.getIntervalSeconds();
    telnetEnabled = cfg.isEnabled();

    closeTask(telnetTask);
    if (!telnetEnabled) {
      telnetTask = null;
      log.info("TELNET task disabled.");
      return;
    }

    log.info("Scheduling TELNET task.");
    telnetTask =
        scheduleTask(
            telnetIntervalSeconds,
            () -> {
              var results = monitoringExecutor.getTelnetResults(devices);
              webClientService.sendResult(results, TelnetResult.class);
            });
  }

  private ScheduledFuture<?> scheduleTask(int intervalSeconds, Runnable task) {
    return scheduler.scheduleWithFixedDelay(
        task, Instant.now(), Duration.ofSeconds(intervalSeconds));
  }

  private void closeTask(ScheduledFuture<?> task) {
    if (task != null && !task.isCancelled()) {
      task.cancel(false);
    }
  }
}
