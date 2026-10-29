package pl.sgorski.nethelt.webapi.exception.domain.discovery;

import pl.sgorski.nethelt.webapi.exception.application.NotAllowedException;

public final class DiscoveryScanNotAllowedException extends NotAllowedException {
  public DiscoveryScanNotAllowedException(String message) {
    super(message);
  }
}
