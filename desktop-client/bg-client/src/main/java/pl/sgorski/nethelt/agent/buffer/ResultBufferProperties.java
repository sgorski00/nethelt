package pl.sgorski.nethelt.agent.buffer;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "buffer")
public record ResultBufferProperties(
    String path, @Positive int maxSize, @Positive int flushBatchSize) {}
