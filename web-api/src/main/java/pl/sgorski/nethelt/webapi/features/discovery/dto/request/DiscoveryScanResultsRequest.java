package pl.sgorski.nethelt.webapi.features.discovery.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record DiscoveryScanResultsRequest(
    @NotNull(message = "Results cannot be empty")
        List<@NotNull(message = "Result cannot be empty") @Valid DiscoveryScanResultRequest>
            results) {}
