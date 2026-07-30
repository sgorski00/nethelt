package pl.sgorski.nethelt.webapi.exception.domain.device;

import pl.sgorski.nethelt.webapi.exception.application.NotFoundException;

public final class DeviceNotFoundException extends NotFoundException {
  public DeviceNotFoundException() {
    super("Selected device not found");
  }
}
