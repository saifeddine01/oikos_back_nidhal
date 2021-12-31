package oikos.app.common.apis;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.exceptions.InternalServerError;
import oikos.app.security.VerificationToken;
import oikos.app.users.User;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Created by Mohamed Haamdi on 25/03/2021.
 */
@AllArgsConstructor @Slf4j public class EmailServiceImpl
  implements EmailService {
  private final String SERVER_PATH;
  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;

  @Async @Override
  public void sendUserVerificationEmail(User user, VerificationToken token) {
    log.info("This should send verification email to user {} with token {}",
      user.getId(), token.getToken());
    MimeMessagePreparator messagePreparator = mimeMessage -> {
      final var messageHelper = new MimeMessageHelper(mimeMessage);
      messageHelper.setFrom("Oikos");
      messageHelper.setTo(user.getEmail());
      messageHelper.setSubject("Oikos Email activation");
      String content = buildVerificationEmail(token.getToken());
      messageHelper.setText(content, true);
    };
    try {
      mailSender.send(messagePreparator);
    } catch (MailException e) {
      log.error("emailimpl.sendverificationemail", e);
      throw new InternalServerError(
        "Our mail servers are down and can't process your signup attempt right now. Please try again later.");
    }
  }

  @Override
  public void sendPasswordResetEmail(User user, VerificationToken token) {
    log.info("This should send verification email to user {} with token {}",
      user.getId(), token.getToken());
  }

  private String buildVerificationEmail(String token) {
    final var context = new Context();
    final var btnLink = SERVER_PATH + "/security/mailconfirm/" + token;
    context.setVariable("btnLink", btnLink);
    return templateEngine.process("emailverification", context);
  }
}
