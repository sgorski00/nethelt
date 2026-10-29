package pl.sgorski.nethelt.webapi.exception.domain.discovery;

import pl.sgorski.nethelt.webapi.exception.application.NotFoundException;

public final class DiscoveryScanNotFoundException extends NotFoundException {
  public DiscoveryScanNotFoundException() {
    super("The network has not been scanned yet");
  }
}
