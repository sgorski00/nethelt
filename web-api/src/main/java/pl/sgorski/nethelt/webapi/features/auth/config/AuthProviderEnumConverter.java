package pl.sgorski.nethelt.webapi.features.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import pl.sgorski.nethelt.webapi.features.auth.domain.AuthProvider;

@Configuration
public class AuthProviderEnumConverter implements Converter<String, AuthProvider> {
    @Override
    public AuthProvider convert(String source) {
        return AuthProvider.fromString(source);
    }
}
