package pl.sgorski.nethelt.webapi.exception.domain.agent;

import pl.sgorski.nethelt.webapi.exception.application.ValidationFailedException;

public final class AgentValidationFailedException extends ValidationFailedException {
  public AgentValidationFailedException(String cause) {
    super("Agent validation failed: " + cause);
  }
}
