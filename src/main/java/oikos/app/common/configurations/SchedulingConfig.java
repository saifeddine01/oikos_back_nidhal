package oikos.app.common.configurations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Date;

/** Created by Mohamed Haamdi on 27/04/2021. */
@Configuration
@EnableScheduling
@EnableAsync
@RequiredArgsConstructor
@Slf4j
public class SchedulingConfig {
  private final TaskScheduler scheduler;
  /**
   * Set a task to be executed in the future.
   *
   * @param task Runnable containing the task to execute.
   * @param date java.util.Date instance containing the moment to run the task.
   */
  @Async
  public void executeTask(Runnable task, Date date) {
    log.info("Task {} has been scheduled for {}", task, date);
    scheduler.schedule(task, date);
  }
}
