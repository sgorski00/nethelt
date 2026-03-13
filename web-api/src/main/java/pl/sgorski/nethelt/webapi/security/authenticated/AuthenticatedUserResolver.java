package pl.sgorski.nethelt.webapi.security.authenticated;

import org.springframework.security.core.Authentication;
import pl.sgorski.nethelt.webapi.features.user.domain.User;

public interface AuthenticatedUserResolver {
    User requireUser(Authentication authentication);
}

