package pl.sgorski.nethelt.webapi.features.monitoring_result.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record MonitoringResultBatchAddRequest(
    @NotEmpty(message = "Batch must contain at least one measurement result")
        @Size(max = MAX_SIZE, message = "Batch can contain at most " + MAX_SIZE + " results")
        List<@NotNull @Valid MonitoringResultAddRequest> results) {

  public static final int MAX_SIZE = 500;
}
