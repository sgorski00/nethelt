package pl.sgorski.nethelt.agent.webclient.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record MonitoringResultBatchRequest(
    @NotEmpty @Size(max = WEB_API_MAX_SIZE) List<@NotNull @Valid MonitoringResultRequest> results) {

  public static final int WEB_API_MAX_SIZE = 500;
}
