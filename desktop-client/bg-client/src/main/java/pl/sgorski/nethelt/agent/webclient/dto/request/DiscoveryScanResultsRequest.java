package pl.sgorski.nethelt.agent.webclient.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record DiscoveryScanResultsRequest(
    @NotNull List<@Valid DiscoveryScanResultRequest> results) {}
