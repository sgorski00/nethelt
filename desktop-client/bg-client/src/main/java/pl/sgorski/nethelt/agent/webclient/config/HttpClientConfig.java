package pl.sgorski.nethelt.agent.webclient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;
import pl.sgorski.nethelt.agent.webclient.api.auth.AgentAuthClient;
import pl.sgorski.nethelt.agent.webclient.api.web.AgentClient;
import pl.sgorski.nethelt.agent.webclient.api.web.DeviceClient;
import pl.sgorski.nethelt.agent.webclient.api.web.MonitoringResultClient;
import pl.sgorski.nethelt.agent.webclient.api.web.NetworkConfigClient;

@Configuration
@ImportHttpServices(
    group = "web-api",
    types = {
      AgentAuthClient.class,
      AgentClient.class,
      DeviceClient.class,
      NetworkConfigClient.class,
      MonitoringResultClient.class
    })
public class HttpClientConfig {

  @Bean
  RestClientHttpServiceGroupConfigurer webApiServiceGroupConfigurer(
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
