package pl.sgorski.nethelt.agent.webclient.api.web;

import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(url = "/client/agent")
public interface AgentClient {

  @PostExchange("/heartbeat")
  void heartbeat();
}
