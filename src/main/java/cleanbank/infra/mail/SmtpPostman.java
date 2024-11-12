package cleanbank.infra.mail;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
class SmtpPostman implements Postman {

  private final JavaMailSender mailSender;

  public SmtpPostman(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  @Override
  public void deliver(SimpleMailMessage mail) {
    mailSender.send(mail);
  }
}
