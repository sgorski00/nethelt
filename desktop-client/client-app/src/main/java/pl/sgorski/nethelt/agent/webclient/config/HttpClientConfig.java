package pl.sgorski.nethelt.agent.webclient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;
import pl.sgorski.nethelt.agent.webclient.api.DeviceClient;
import pl.sgorski.nethelt.agent.webclient.api.MonitoringResultClient;
import pl.sgorski.nethelt.agent.webclient.api.NetworkConfigClient;

@Configuration
@ImportHttpServices(
    group = "web-api",
    types = {DeviceClient.class, NetworkConfigClient.class, MonitoringResultClient.class})
public class HttpClientConfig {

  @Bean
  RestClientHttpServiceGroupConfigurer httpServiceGroupConfigurer(
      AuthorizationInterceptor authorizationInterceptor,
      WebApiResponseErrorHandler responseErrorHandler) {
    return groups ->
        groups
            .filterByName("web-api")
            .forEachClient(
                (_, clientBuilder) ->
                    clientBuilder
                        .requestInterceptor(authorizationInterceptor)
                        .defaultStatusHandler(responseErrorHandler));
  }
}
