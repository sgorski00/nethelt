package pl.sgorski.nethelt.webapi.web.converter;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationChannel;

@Configuration
public class NotificationChannelEnumConverter implements Converter<String, NotificationChannel> {
  @Override
  public NotificationChannel convert(String source) {
    return NotificationChannel.fromString(source);
  }
}
