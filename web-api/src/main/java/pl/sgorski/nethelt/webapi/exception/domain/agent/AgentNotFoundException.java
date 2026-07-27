package pl.sgorski.nethelt.webapi.exception.domain.agent;

import pl.sgorski.nethelt.webapi.exception.application.NotFoundException;

public final class AgentNotFoundException extends NotFoundException {
  public AgentNotFoundException() {
    super("Agent not found for this network");
  }
}
