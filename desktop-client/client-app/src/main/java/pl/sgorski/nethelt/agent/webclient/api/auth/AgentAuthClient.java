package pl.sgorski.nethelt.agent.webclient.api.auth;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import pl.sgorski.nethelt.agent.webclient.dto.request.AgentAuthRequest;
import pl.sgorski.nethelt.agent.webclient.dto.response.AgentAuthResponse;

@HttpExchange(url = "/agent")
public interface AgentAuthClient {
  @PostExchange("/authenticate")
  AgentAuthResponse authenticate(@Valid @RequestBody AgentAuthRequest request);
}
