package pl.sgorski.nethelt.webapi.notification.infrastructure.mail.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.webapi.notification.infrastructure.mail.EmailSender;

@Slf4j
@Profile("test")
@Component
public class NoopEmailSender implements EmailSender {
  @Override
  public void send(String to, String subject, String body) {
    log.info("NoopEmailSender: Sending email to: {}, subject: {}, body: {}", to, subject, body);
  }
}
