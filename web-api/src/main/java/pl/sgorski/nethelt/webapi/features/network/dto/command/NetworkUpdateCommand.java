package pl.sgorski.nethelt.webapi.features.network.dto.command;

import org.jspecify.annotations.Nullable;

public record NetworkUpdateCommand(String name, @Nullable String description) {}
