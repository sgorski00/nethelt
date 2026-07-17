package pl.sgorski.nethelt.webapi.notification.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;
import pl.sgorski.nethelt.webapi.exception.notification.NotificationChannelNotFoundException;

public enum NotificationChannel {
  EMAIL,
  WEBSOCKET;

  @JsonCreator
  public static NotificationChannel fromString(String value) {
    return Arrays.stream(values())
        .filter(channel -> channel.name().equalsIgnoreCase(value.trim()))
        .findFirst()
        .orElseThrow(NotificationChannelNotFoundException::new);
  }
}
