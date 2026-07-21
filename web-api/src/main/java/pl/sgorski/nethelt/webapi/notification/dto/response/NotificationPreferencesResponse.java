package pl.sgorski.nethelt.webapi.notification.dto.response;

import java.util.Set;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationChannel;

public record NotificationPreferencesResponse(
    Long userId, Set<NotificationChannel> enabledChannels) {}
