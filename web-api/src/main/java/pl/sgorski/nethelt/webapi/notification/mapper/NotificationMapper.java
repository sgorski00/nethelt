package pl.sgorski.nethelt.webapi.notification.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationPreferences;
import pl.sgorski.nethelt.webapi.notification.dto.response.NotificationPreferencesResponse;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

  @Mapping(target = "userId", source = "user.id")
  NotificationPreferencesResponse toResponse(NotificationPreferences preferences);
}
