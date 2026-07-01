package pl.sgorski.nethelt.webapi.exception.http.client;

public final class GithubApiException extends ApiClientException {
  public GithubApiException(String message, int statusCode) {
    super(message, statusCode);
  }
}
