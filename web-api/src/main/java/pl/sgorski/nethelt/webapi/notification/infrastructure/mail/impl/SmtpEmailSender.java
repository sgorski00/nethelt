package pl.sgorski.nethelt.webapi.notification.infrastructure.mail.impl;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.webapi.notification.infrastructure.mail.EmailSender;

@Slf4j
@Profile("!test")
@Component
@RequiredArgsConstructor
public class SmtpEmailSender implements EmailSender {

  private final JavaMailSender sender;

  @Override
  public void send(String to, String subject, String body) {
    try {
      var message = sender.createMimeMessage();
      message.addRecipients(Message.RecipientType.TO, to);
      message.setSubject(subject);
      message.setText(body);
      sender.send(message);
    } catch (MessagingException e) {
      log.error("Failed to send email to {} with subject {}: {}", to, subject, e.getMessage(), e);
    }
  }
}
