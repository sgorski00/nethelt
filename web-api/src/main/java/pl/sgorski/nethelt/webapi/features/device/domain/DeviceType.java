package pl.sgorski.nethelt.webapi.features.device.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeviceType {
  LAN_CLIENT("Client LAN"),
  WIFI_CLIENT("Client WiFi"),
  NETWORK_DEVICE("Network Infrastructure");

  private final String displayName;
}
