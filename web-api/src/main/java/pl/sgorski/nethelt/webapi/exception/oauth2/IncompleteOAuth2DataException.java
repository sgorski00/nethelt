package pl.sgorski.nethelt.webapi.exception.oauth2;

public final class IncompleteOAuth2DataException extends RuntimeException {

  public IncompleteOAuth2DataException() {
    super(
        "An internal error - required data was not provided by the OAuth2 provider. Please contact support.");
  }
}
