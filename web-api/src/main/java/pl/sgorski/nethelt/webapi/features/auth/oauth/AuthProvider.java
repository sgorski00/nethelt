package pl.sgorski.nethelt.webapi.features.auth.oauth;

import com.fasterxml.jackson.annotation.JsonCreator;
import pl.sgorski.nethelt.webapi.exception.application.ProviderNotFoundException;

import java.util.Arrays;

public enum AuthProvider {
    GOOGLE,
    GITHUB;

    @JsonCreator
    public static AuthProvider fromString(String value) {
        return Arrays.stream(values())
                .filter(provider -> provider.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new ProviderNotFoundException("Invalid provider: " + value));
    }
}
