package oikos.app.security;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/** Created by Mohamed Haamdi on 27/03/2021. */
@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class TokensPurgeTask {
  private final VerificationTokenRepo repo;
  // All the expired tokens are deleted by midnight.
  @Scheduled(cron = "0 0 0 * * ?")
  public void purgeExpiredTokens() {
    log.info("Purging all expired verification tokens at : {}", Instant.now());
    repo.deleteAllExpiredSince(Instant.now());
  }
}
