package pl.sgorski.nethelt.webapi.features.auth.domain;

public interface OAuthUserInfo {
    AuthProvider getAuthProvider();
    String getProviderId();
    String getEmail();
}
