package pl.sgorski.nethelt.webapi.notification.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.sgorski.nethelt.webapi.notification.domain.Notification;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationPreferences;
import pl.sgorski.nethelt.webapi.notification.dto.response.NotificationPreferencesResponse;
import pl.sgorski.nethelt.webapi.notification.dto.response.NotificationResponse;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

  @Mapping(target = "userId", source = "user.id")
  NotificationPreferencesResponse toResponse(NotificationPreferences preferences);

  NotificationResponse toResponse(Notification notification);
}
