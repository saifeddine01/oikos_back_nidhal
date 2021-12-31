package oikos.app.common.apis;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oikos.app.security.VerificationToken;
import oikos.app.users.User;

@AllArgsConstructor @Slf4j public class MockEmailService
  implements EmailService {
  @Override
  public void sendUserVerificationEmail(User user, VerificationToken token) {
    log.info("This should send verification email to user {} with token {}",
      user.getId(), token.getToken());
  }

  @Override
  public void sendPasswordResetEmail(User user, VerificationToken token) {
    log.info("This should send verification email to user {} with token {}",
      user.getId(), token.getToken());
  }
}
