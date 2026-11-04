package pl.sgorski.nethelt.agent.buffer;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import pl.sgorski.nethelt.agent.webclient.dto.request.MonitoringResultBatchRequest;

@Validated
@ConfigurationProperties(prefix = "buffer")
public record ResultBufferProperties(
    String path,
    @Positive int maxSize,
    @Positive @Max(MonitoringResultBatchRequest.WEB_API_MAX_SIZE) int flushBatchSize) {}
