package pl.sgorski.nethelt.webapi.notification.infrastructure.mail.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import pl.sgorski.nethelt.webapi.exception.notification.EmailSendingException;

@ExtendWith(MockitoExtension.class)
public class SmtpEmailSenderTests {

  @Mock private JavaMailSender mailSender;
  @InjectMocks private SmtpEmailSender emailSender;

  private MimeMessage mimeMessage;

  @Test
  void send_shouldCreateAndSendMimeMessage() throws Exception {
    mimeMessage = new MimeMessage((Session) null);
    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

    emailSender.send("john@example.com", "Subject", "Body");

    verify(mailSender).createMimeMessage();
    verify(mailSender).send(mimeMessage);
    assertTrue(
        Arrays.stream(mimeMessage.getRecipients(Message.RecipientType.TO))
            .anyMatch(address -> address.toString().equals("john@example.com")));
    assertEquals("Subject", mimeMessage.getSubject());
    assertEquals("Body", mimeMessage.getContent());
  }

  @Test
  void send_shouldThrow_whenMessagingExceptionOccurs() throws Exception {
    mimeMessage = mock(MimeMessage.class);
    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    doThrow(MessagingException.class).when(mimeMessage).addRecipients(any(), eq("not-valid-email"));

    assertThrows(
        EmailSendingException.class, () -> emailSender.send("not-valid-email", "Subject", "Body"));
    verify(mailSender, never()).send(any(MimeMessage.class));
  }
}
