package pl.sgorski.nethelt.agent.scheduler;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sgorski.nethelt.agent.webclient.WebClientService;
import pl.sgorski.nethelt.agent.executor.ResultProvider;
import pl.sgorski.nethelt.model.Device;
import pl.sgorski.nethelt.model.NetworkConfig;
import pl.sgorski.nethelt.model.Operation;
import pl.sgorski.nethelt.model.PingResult;
import pl.sgorski.nethelt.model.TelnetResult;

/**
 * Manages scheduled tasks for network operations based on configurations fetched from a web service.
 */
public class WebScheduledTaskManager implements ScheduledTaskManager {

  private static final int CONFIG_UPDATE_INTERVAL_SEC = 60;
  private static final Logger LOG = LoggerFactory.getLogger(WebScheduledTaskManager.class);

  /**
   * Scheduled executor. It uses threads equal to number of operations.
   */
  private final ScheduledExecutorService scheduler;
  private final WebClientService webClientService;
  private final ResultProvider resultProvider;

  private ScheduledFuture<?> pingTask;
  private int pingInterval = -1; //actual interval in seconds
  private boolean pingEnabled = false; //actual status
  private ScheduledFuture<?> telnetTask;
  private int telnetInterval = -1; //actual interval in seconds
  private boolean telnetEnabled = false; //actual status

  public WebScheduledTaskManager(WebClientService webClientService, ResultProvider resultProvider) {
    int numberOfOperations = Operation.values().length;
    this.scheduler = Executors.newScheduledThreadPool(numberOfOperations);
    this.webClientService = webClientService;
    this.resultProvider = resultProvider;
    LOG.debug("Scheduler initialized with {} threads for operations", numberOfOperations);
  }

  /**
   * Starts the scheduled task manager, which periodically fetches network configurations and updates the scheduled tasks accordingly.
   */
  public void start() {
    LOG.info("Starting Scheduled Task Manager");
    scheduler.scheduleWithFixedDelay(this::updateTasks, 0, CONFIG_UPDATE_INTERVAL_SEC, TimeUnit.SECONDS);
  }

  private void updateTasks() {
    try {
      Set<NetworkConfig> configs = webClientService.fetchNetworkConfig();
      Set<Device> devices = webClientService.fetchDevices();

      for (NetworkConfig cfg : configs) {
        switch (cfg.getOperation()) {
          case PING -> handlePing(cfg, devices);
          case TELNET -> handleTelnet(cfg, devices);
          default -> LOG.warn("Unsupported operation: {}", cfg.getOperation());
        }
      }
    } catch (Exception e) {
      LOG.error("Error while updating tasks: {}", e.getMessage(), e);
    }
  }

  private void handlePing(NetworkConfig cfg, Set<Device> devices) {
    if (cfg.isNotChanged(pingEnabled, pingInterval)) return;

    LOG.info("Configuration change detected for PING operation.");
    pingInterval = cfg.getIntervalSeconds();
    pingEnabled = cfg.isEnabled();

    closeTask(pingTask);
    if (pingEnabled) {
      LOG.info("Rescheduling PING task (enabled).");
      Runnable runnable = () -> {
        Set<PingResult> results = resultProvider.getPingResults(devices);
        webClientService.sendResult(results, PingResult.class);
      };
      pingTask = rescheduleTask(pingTask, pingInterval, runnable);
    } else {
      LOG.info("PING task disabled.");
    }
  }

  private void handleTelnet(NetworkConfig cfg, Set<Device> devices) {
    if (cfg.isNotChanged(telnetEnabled, telnetInterval))
      return;

    LOG.info("Configuration change detected for TELNET operation.");
    telnetInterval = cfg.getIntervalSeconds();
    telnetEnabled = cfg.isEnabled();

    closeTask(telnetTask);
    if (telnetEnabled) {
      LOG.info("Rescheduling TELNET task (enabled).");
      Runnable runnable = () -> {
        Set<TelnetResult> results = resultProvider.getTelnetResults(devices);
        webClientService.sendResult(results, TelnetResult.class);
      };
      telnetTask = rescheduleTask(telnetTask, telnetInterval, runnable);
    } else {
      LOG.info("TELNET task disabled.");
    }
  }

  private ScheduledFuture<?> rescheduleTask(ScheduledFuture<?> currentTask, int intervalSec, Runnable task) {
    closeTask(currentTask);
    return scheduler.scheduleWithFixedDelay(task, 0, intervalSec, TimeUnit.SECONDS);
  }

  public void stop() {
    LOG.info("Stopping Scheduled Task Manager");
    closeTask(pingTask);
    closeTask(telnetTask);
    scheduler.shutdown();
  }

  private void closeTask(ScheduledFuture<?> task) {
    if (Objects.nonNull(task) && !task.isCancelled()) {
      task.cancel(false);
    }
  }
}
