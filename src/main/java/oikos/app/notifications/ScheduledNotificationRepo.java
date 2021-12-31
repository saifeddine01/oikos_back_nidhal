package oikos.app.notifications;

import org.springframework.data.jpa.repository.JpaRepository;

/** Created by Mohamed Haamdi on 27/04/2021. */
public interface ScheduledNotificationRepo extends JpaRepository<ScheduledNotification, String> {}
