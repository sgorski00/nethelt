package pl.sgorski.nethelt.webapi.features.network.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;

public record NetworkUpdateRequest(@NotBlank String name, @Nullable String description) {}
