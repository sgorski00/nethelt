package pl.sgorski.nethelt.webapi.exception.domain.agent;

import pl.sgorski.nethelt.webapi.exception.application.AlreadyExistsException;

public final class AgentAlreadyExistsException extends AlreadyExistsException {
  public AgentAlreadyExistsException() {
    super("Couldn't save new network agent because it is already created.");
  }
}
