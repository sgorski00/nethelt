package pl.sgorski.nethelt.webapi.security.authenticated;

import org.springframework.security.core.Authentication;

public interface AuthenticatedUserResolver {
    Long requireUserId(Authentication authentication);
}

