package pl.sgorski.nethelt.webapi.notification.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification.delivery")
public record NotificationDeliveryProperties(
    int maxAttempts, Duration retryBackoff, int retryBatchSize, Duration retention) {}
