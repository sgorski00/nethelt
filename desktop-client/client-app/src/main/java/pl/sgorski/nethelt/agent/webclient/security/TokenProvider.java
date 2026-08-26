package pl.sgorski.nethelt.agent.webclient.security;

import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class TokenProvider {

  // todo: implement real pat to jwt logic.
  public Optional<String> getToken() {
    return Optional.of("mock-token");
  }
}
