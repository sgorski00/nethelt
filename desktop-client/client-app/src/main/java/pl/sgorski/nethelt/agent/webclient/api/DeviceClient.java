package pl.sgorski.nethelt.agent.webclient.api;

import java.util.Set;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import pl.sgorski.nethelt.agent.model.Device;

@HttpExchange(url = "/devices")
public interface DeviceClient {
  @GetExchange
  Set<Device> getDevices();
}
