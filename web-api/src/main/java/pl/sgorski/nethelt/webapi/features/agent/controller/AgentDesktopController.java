package pl.sgorski.nethelt.webapi.features.agent.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.sgorski.nethelt.webapi.features.agent.service.AgentDesktopService;

@RestController
@RequestMapping(value = "/agent", version = "1")
@RequiredArgsConstructor
public class AgentDesktopController {

  private final AgentDesktopService agentDesktopService;

  @PostMapping("/heartbeat")
  public ResponseEntity<Void> heartbeat() {
    // TODO: get agent id from jwt token
    // maybe via authenticatedUserResolver?
    // can be done after implementing AgentPrincipal
    // secure /agent only for authenticated agents
    agentDesktopService.heartbeat(1L);
    return ResponseEntity.noContent().build();
  }

  // todo: implement more desktop - web api endpoints here
}
