package pl.sgorski.nethelt.webapi.exception.oauth2;

public final class InvalidOAuth2UserInfoException extends RuntimeException {

  public InvalidOAuth2UserInfoException(String missingField) {
    super("Couldn't link with this provider. Missing required field: " + missingField);
  }
}
