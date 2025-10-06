package pl.gorski.nethelt.agent;

import pl.gorski.nethelt.agent.scheduler.WebScheduledTaskManager;
import pl.gorski.nethelt.agent.webclient.WebClientService;
import pl.sgorski.nethelt.agent.executor.ResultProvider;
import pl.sgorski.nethelt.agent.scheduler.ScheduledTaskManager;
import pl.sgorski.nethelt.agent.serialization.SerializationController;
import pl.sgorski.nethelt.agent.serialization.impl.DefaultSerializationController;

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
