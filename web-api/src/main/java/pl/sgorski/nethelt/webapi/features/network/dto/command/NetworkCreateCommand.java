package pl.sgorski.nethelt.webapi.features.network.dto.command;

import org.jspecify.annotations.Nullable;

public record NetworkCreateCommand(Long userId, String name, @Nullable String description) {}
