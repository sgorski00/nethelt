package pl.sgorski.nethelt.webapi.notification.dto.response;

import java.time.Instant;
import org.jspecify.annotations.Nullable;

public record NotificationResponse(
    Long id, String title, String content, Instant createdAt, @Nullable Instant readAt) {}
