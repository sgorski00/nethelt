package pl.sgorski.nethelt.webapi.exception.domain.network;

import pl.sgorski.nethelt.webapi.exception.application.NotFoundException;

public final class NetworkNotFoundException extends NotFoundException {
  public NetworkNotFoundException() {
    super("Network not found");
  }
}
