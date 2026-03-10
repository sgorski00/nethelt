package pl.sgorski.nethelt.webapi.features.user.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import pl.sgorski.nethelt.webapi.exception.RoleNotFoundException;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum Role implements GrantedAuthority {
    USER("User"),
    ADMIN("Admin");

    private final String displayName;

    @JsonCreator
    public static Role fromString(String value) {
        return Arrays.stream(values())
                .filter(role -> role.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new RoleNotFoundException("Invalid role: " + value));
    }

    @Override
    public String getAuthority() {
        return "ROLE_" + name();
    }
}
