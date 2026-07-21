package pl.sgorski.nethelt.webapi.notification.infrastructure.mail;

public interface EmailSender {
  void send(String to, String subject, String body);
}
