package pl.sgorski.nethelt.webapi.security.session;

import jakarta.servlet.http.HttpSession;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import static pl.sgorski.nethelt.webapi.security.session.OAuthSessionAttributes.OAUTH_LINK_USER_ID;
import static pl.sgorski.nethelt.webapi.security.session.OAuthSessionAttributes.OAUTH_MODE;

@Service
public final class OAuth2SessionService {

    public boolean isLinkMode(HttpSession session) {
        return "link".equals(session.getAttribute(OAUTH_MODE.getAttributeName()));
    }

    public @Nullable Long getOAuthLinkUserId(HttpSession session) {
        return (Long) session.getAttribute(OAUTH_LINK_USER_ID.getAttributeName());
    }

    public void clearOAuthAttributes(HttpSession session) {
        session.removeAttribute(OAUTH_MODE.getAttributeName());
        session.removeAttribute(OAUTH_LINK_USER_ID.getAttributeName());
    }
}

