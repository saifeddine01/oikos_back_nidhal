package oikos.app.common.apis;

import oikos.app.security.VerificationToken;
import oikos.app.users.User;

public interface EmailService {
  void sendUserVerificationEmail(User user, VerificationToken token);
  void sendPasswordResetEmail(User user, VerificationToken token);
}
