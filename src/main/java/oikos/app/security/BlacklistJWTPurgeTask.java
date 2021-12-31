package oikos.app.security;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oikos.app.common.configurations.AppProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class BlacklistJWTPurgeTask {
  private final AppProperties appProperties;
  private final BlacklistedJWTRepo repo;
  // All the expired tokens are deleted by midnight.
  @Scheduled(cron = "0 0 0 * * ?")
  public void purgeExpiredTokens() {
    log.info("Purging all expired blacklisted JWT tokens at : {}", Instant.now());
    repo.deleteAllPurgable(Instant.now());
  }
}
