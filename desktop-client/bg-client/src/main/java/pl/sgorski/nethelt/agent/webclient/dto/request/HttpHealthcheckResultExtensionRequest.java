package pl.sgorski.nethelt.agent.webclient.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.jspecify.annotations.Nullable;

public record HttpHealthcheckResultExtensionRequest(
    @Nullable @Min(100) @Max(599) Integer statusCode) implements MonitoringResultExtensionRequest {}
