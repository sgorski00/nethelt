package pl.sgorski.nethelt.webapi.features.auth.oauth;

public interface OAuthUserInfo {
  AuthProvider getProvider();

  String getProviderId();

  String getEmail();
}
