package pl.sgorski.nethelt.agent;

import pl.sgorski.nethelt.agent.scheduler.WebScheduledTaskManager;
import pl.sgorski.nethelt.agent.webclient.WebClientService;
import pl.sgorski.nethelt.agent.executor.ResultProvider;
import pl.sgorski.nethelt.agent.scheduler.ScheduledTaskManager;
import pl.sgorski.nethelt.agent.serialization.SerializationController;
import pl.sgorski.nethelt.agent.serialization.impl.DefaultSerializationController;

/**
 * Main application class for the Nethelt Background agent.
 * It starts in the bg-client as a scheduler and fetch data from the web service.
 * After executing operations it sends results back to the web service.
 */
public class App {

  public static void main(String[] args) {
    SerializationController serializer = new DefaultSerializationController();
    WebClientService webClientService = new WebClientService(serializer);
    ResultProvider resultProvider = new ResultProvider();
    ScheduledTaskManager taskManager = new WebScheduledTaskManager(webClientService, resultProvider);

    taskManager.start();

    Runtime.getRuntime().addShutdownHook(new Thread(taskManager::stop));
  }
}
