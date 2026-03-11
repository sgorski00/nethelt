package pl.sgorski.nethelt.webapi.features.auth.domain;

public interface OAuthUserInfo {
    AuthProvider getProvider();
    String getProviderId();
    String getEmail();
}
