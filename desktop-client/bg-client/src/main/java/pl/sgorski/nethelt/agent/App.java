package pl.sgorski.nethelt.agent;

import pl.sgorski.nethelt.agent.config.WebClientSingleton;
import pl.sgorski.nethelt.agent.scheduler.WebScheduledTaskManager;
import pl.sgorski.nethelt.agent.webclient.WebClientService;
import pl.sgorski.nethelt.agent.executor.ResultProvider;
import pl.sgorski.nethelt.agent.serialization.impl.DefaultSerializationController;

/**
 * Main application class for the Nethelt Background agent.
 * It starts in the bg-client as a scheduler and fetch data from the web service.
 * After executing operations it sends results back to the web service.
 */
public class App {

  static void main() {
    var webClient = WebClientSingleton.INSTANCE.getHttpClient();
    var serializer = new DefaultSerializationController();
    var webClientService = new WebClientService(webClient, serializer);
    var resultProvider = new ResultProvider();
    var taskManager = new WebScheduledTaskManager(webClientService, resultProvider);

    taskManager.start();

    Runtime.getRuntime().addShutdownHook(new Thread(taskManager::stop));
  }
}
