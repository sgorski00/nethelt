package pl.sgorski.nethelt.webapi.features.monitoring_result.dto.request.extension;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.request.MonitoringResultExtensionRequest;

public record HttpHealthcheckResultExtensionRequest(
    @Nullable
        @Min(value = 100, message = "Status code cannot be lesser than 100")
        @Max(value = 599, message = "Status code cannot be greater than 599")
        Integer statusCode)
    implements MonitoringResultExtensionRequest {}
