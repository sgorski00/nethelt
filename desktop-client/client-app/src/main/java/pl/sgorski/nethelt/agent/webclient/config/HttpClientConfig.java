package pl.sgorski.nethelt.agent.webclient.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;
import pl.sgorski.nethelt.agent.webclient.api.DeviceClient;
import pl.sgorski.nethelt.agent.webclient.api.MonitoringResultClient;
import pl.sgorski.nethelt.agent.webclient.api.NetworkConfigClient;

@Configuration
@ImportHttpServices(
    group = "web-api",
    types = {DeviceClient.class, NetworkConfigClient.class, MonitoringResultClient.class})
public class HttpClientConfig {}
