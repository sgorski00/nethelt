package pl.gorski.nethelt.agent.scheduler;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import pl.gorski.nethelt.agent.webclient.WebClientService;
import pl.sgorski.nethelt.agent.executor.ResultProvider;
import pl.sgorski.nethelt.agent.scheduler.ScheduledTaskManager;
import pl.sgorski.nethelt.core.model.Device;
import pl.sgorski.nethelt.core.model.NetworkConfig;
import pl.sgorski.nethelt.core.model.Operation;
import pl.sgorski.nethelt.core.model.PingResult;
import pl.sgorski.nethelt.core.model.TelnetResult;

/**
 * Manages scheduled tasks for network operations based on configurations fetched from a web service.
 */
public class WebScheduledTaskManager implements ScheduledTaskManager {

  /** Scheduled executor. It uses threads equal to number of operations. */
  private final ScheduledExecutorService scheduler;
  private final WebClientService webClientService;
  private final ResultProvider resultProvider;

  private ScheduledFuture<?> pingTask;
  private int pingInterval = -1; //actual interval in seconds
  private ScheduledFuture<?> telnetTask;
  private int telnetInterval = -1; //

  private static final int CONFIG_UPDATE_INTERVAL_SEC = 60;

  public WebScheduledTaskManager(WebClientService webClientService, ResultProvider resultProvider) {
    int numberOfOperations = Operation.values().length;
    this.scheduler = Executors.newScheduledThreadPool(numberOfOperations);
    this.webClientService = webClientService;
    this.resultProvider = resultProvider;
  }

  /**
   * Starts the scheduled task manager, which periodically fetches network configurations and updates the scheduled tasks accordingly.
   */
  public void start() {
    scheduler.scheduleWithFixedDelay(this::updateTasks, 0, CONFIG_UPDATE_INTERVAL_SEC, TimeUnit.SECONDS);
  }

  private void updateTasks() {
    try {
      Set<NetworkConfig> configs = webClientService.fetchNetworkConfig();
      Set<Device> devices = webClientService.fetchDevices();

      for (NetworkConfig cfg : configs) {
        if (!cfg.isEnabled()) continue;

        Runnable runnable;
        switch (cfg.getOperation()) {
        case PING:
          if(cfg.getIntervalSeconds() == pingInterval) continue;

          runnable = () -> {
            Set<PingResult> results = resultProvider.getPingResults(devices);
            webClientService.sendResult(results, PingResult.class);
          };
          pingInterval = cfg.getIntervalSeconds();
          pingTask = rescheduleTask(pingTask, pingInterval, runnable);
          break;
        case TELNET:
          if(cfg.getIntervalSeconds() == telnetInterval) continue;

          runnable = () -> {
            Set<TelnetResult> results = resultProvider.getTelnetResults(devices);
            webClientService.sendResult(results, TelnetResult.class);
          };
          telnetInterval = cfg.getIntervalSeconds();
          telnetTask = rescheduleTask(telnetTask, telnetInterval, runnable);
          break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private ScheduledFuture<?> rescheduleTask(ScheduledFuture<?> currentTask, int intervalSec, Runnable task) {
    if (currentTask != null && !currentTask.isCancelled()) {
      currentTask.cancel(false);
    }
    return scheduler.scheduleWithFixedDelay(task, 0, intervalSec, TimeUnit.SECONDS);
  }

  public void stop() {
    if (pingTask != null) pingTask.cancel(false);
    if (telnetTask != null) telnetTask.cancel(false);
    scheduler.shutdown();
  }
}
