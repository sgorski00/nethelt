package pl.sgorski.nethelt.webapi.features.user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import pl.sgorski.nethelt.webapi.features.user.domain.Role;

@Configuration
public class RoleEnumConverter implements Converter<String, Role> {
    @Override
    public Role convert(String source) {
        return Role.fromString(source);
    }
}
