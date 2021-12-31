package oikos.app.common.apis;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oikos.app.security.VerificationToken;
import oikos.app.users.User;
import org.springframework.stereotype.Service;

/** Created by Mohamed Haamdi on 25/03/2021. */
@Service
@AllArgsConstructor
@Slf4j
public class SMSService {
  public void sendUserVerificationSMS(User user, VerificationToken token) {
    log.info(
        "This should send verification sms to user {} with token {}",
        user.getId(),
        token.getToken());
  }
}
