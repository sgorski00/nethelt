package pl.sgorski.nethelt.agent.webclient.api;

import jakarta.validation.Valid;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import pl.sgorski.nethelt.agent.webclient.dto.request.AgentAuthRequest;
import pl.sgorski.nethelt.agent.webclient.dto.response.AgentAuthResponse;

@HttpExchange(url = "/agent")
public interface AgentClient {
  @PostExchange("/authenticate")
  AgentAuthResponse authenticate(@Valid AgentAuthRequest request);

  @PostExchange("/heartbeat")
  void heartbeat();
}
