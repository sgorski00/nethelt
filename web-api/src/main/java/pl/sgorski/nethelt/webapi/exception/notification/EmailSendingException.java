package pl.sgorski.nethelt.webapi.exception.notification;

public class EmailSendingException extends RuntimeException {
  public EmailSendingException(Throwable cause) {
    super(cause);
  }
}
