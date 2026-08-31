package pl.sgorski.nethelt.webapi.features.agent.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.sgorski.nethelt.webapi.features.agent.dto.request.AgentAuthRequest;
import pl.sgorski.nethelt.webapi.features.agent.service.AgentDesktopService;
import pl.sgorski.nethelt.webapi.features.auth.dto.response.JwtResponse;
import pl.sgorski.nethelt.webapi.security.agent.AgentAuthentication;

@RestController
@RequestMapping(value = "/agent", version = "1")
@RequiredArgsConstructor
public class AgentDesktopController {

  private final AgentDesktopService agentDesktopService;

  @PostMapping("/heartbeat")
  public ResponseEntity<Void> heartbeat(AgentAuthentication authentication) {
    agentDesktopService.heartbeat(authentication.getPrincipal().agentId());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/authenticate")
  public ResponseEntity<JwtResponse> authenticate(@RequestBody AgentAuthRequest request) {
    var token = agentDesktopService.authenticateAndGenerateToken(request.token());
    return ResponseEntity.status(HttpStatus.CREATED).body(new JwtResponse(token));
  }
}
