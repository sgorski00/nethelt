package pl.sgorski.nethelt.webapi.features.auth.service;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Helper class for managing HTTP response cookies (refresh tokens).
 */
@Component
public final class CookieResponseHelper {

    public static final String REFRESH_TOKEN_COOKIE_KEY = "refreshToken";
    private static final String COOKIE_PATH = "/api";
    private static final String SAME_SITE = "Strict";
    private static final boolean HTTP_ONLY = true;
    private static final boolean SECURE = true;

    /**
     * Creates a refresh token cookie with secure settings.
     *
     * @param tokenValue the token value
     * @param maxAgeSec  maximum age in seconds
     * @return configured ResponseCookie
     */
    public ResponseCookie createRefreshTokenCookie(String tokenValue, long maxAgeSec) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_KEY, tokenValue)
                .httpOnly(HTTP_ONLY)
                .secure(SECURE)
                .path(COOKIE_PATH)
                .maxAge(maxAgeSec)
                .sameSite(SAME_SITE)
                .build();
    }

    /**
     * Creates an empty refresh token cookie (for logout).
     *
     * @return configured ResponseCookie for clearing the cookie
     */
    public ResponseCookie createClearRefreshTokenCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_KEY, "")
                .path(COOKIE_PATH)
                .maxAge(0)
                .build();
    }
}

