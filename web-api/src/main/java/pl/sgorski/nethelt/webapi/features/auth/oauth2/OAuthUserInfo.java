package pl.sgorski.nethelt.webapi.features.auth.oauth2;

public interface OAuthUserInfo {
  AuthProvider getProvider();

  String getProviderId();

  String getEmail();
}
