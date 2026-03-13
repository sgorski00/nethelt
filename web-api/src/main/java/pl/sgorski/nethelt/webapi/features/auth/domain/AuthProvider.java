package pl.sgorski.nethelt.webapi.features.auth.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import pl.sgorski.nethelt.webapi.exception.ProviderNotFoundException;

import java.util.Arrays;

public enum AuthProvider {
    GOOGLE;

    @JsonCreator
    public static AuthProvider fromString(String value) {
        return Arrays.stream(values())
                .filter(provider -> provider.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new ProviderNotFoundException("Invalid provider: " + value));
    }
}
