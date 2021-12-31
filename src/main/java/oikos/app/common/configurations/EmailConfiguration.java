package oikos.app.common.configurations;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.apis.EmailService;
import oikos.app.common.apis.EmailServiceImpl;
import oikos.app.common.apis.MockEmailService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

@AllArgsConstructor @Configuration @Slf4j public class EmailConfiguration {
  private static final String SERVER_PATH = "http://localhost:8080/api/v1";
  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;

  @ConditionalOnProperty(name = "mail.type", havingValue = "mock") @Bean
  public EmailService mockEmailService() {
    return new MockEmailService();
  }

  @ConditionalOnProperty(name = "mail.type", havingValue = "email") @Bean
  public EmailService emailServiceImpl() {
    return new EmailServiceImpl(SERVER_PATH, mailSender, templateEngine);
  }
}
