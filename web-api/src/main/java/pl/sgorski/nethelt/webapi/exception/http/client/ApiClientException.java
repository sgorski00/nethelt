package pl.sgorski.nethelt.webapi.exception.http.client;

public class ApiClientException extends RuntimeException {
  int statusCode;

  public ApiClientException(String message, int statusCode) {
    super(message);
    this.statusCode = statusCode;
  }
}
