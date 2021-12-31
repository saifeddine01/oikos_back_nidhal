package oikos.app.ads;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class UpdateViewsTask {
  private final AdService adService;
  @Scheduled(cron = "0 0 0 * * ?")
  public void updateViewsTask() {
    log.info("Updating view counts for ads still listed at : {}", Instant.now());
    adService.updateViewsTask();
  }
}
