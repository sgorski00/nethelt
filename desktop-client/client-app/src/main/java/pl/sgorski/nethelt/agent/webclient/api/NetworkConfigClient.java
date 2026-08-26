package pl.sgorski.nethelt.agent.webclient.api;

import java.util.Set;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import pl.sgorski.nethelt.agent.model.NetworkConfig;

@HttpExchange(url = "/configs")
public interface NetworkConfigClient {
  @GetExchange
  Set<NetworkConfig> getNetworkConfigs();
}
